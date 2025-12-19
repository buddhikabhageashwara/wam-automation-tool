package wam.automationtool.application.util;

import static wam.automationtool.application.config.AppConstant.ClaimName.FIRST_NAME;
import static wam.automationtool.application.config.AppConstant.ClaimName.IS_SUPER_ADMIN;
import static wam.automationtool.application.config.AppConstant.ClaimName.LAST_NAME;
import static wam.automationtool.application.config.AppConstant.ClaimName.PERMISSION_TYPE_LIST;
import static wam.automationtool.application.config.AppConstant.ClaimName.USER_EMAIL;
import static wam.automationtool.application.config.AppConstant.ClaimName.USER_ID;
import static wam.automationtool.application.config.AppConstant.WAM_AUTOMATION_EXECUTION_TOKEN_EXPIRATION;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import wam.automationtool.application.config.AppConstant;
import wam.automationtool.application.dto.JWTTokenDto;
import wam.automationtool.application.dto.WAMAutomationTokenDto;
import wam.automationtool.application.dto.WAMAutomationUserDetailsDto;

@Component
@Slf4j
@RequiredArgsConstructor
public class WAMAutomationJWTTokenUtil {

  @Value("${wam.automation.token.secret}")
  private String secret;

  /**
   * Retrieves the expiration date from the provided JWT token.
   *
   * @param token the JWT token
   * @return the expiration date of the token
   */
  public Date getExpirationDateFromToken(final String token) {
    return getClaimFromToken(token, Claims::getExpiration);
  }

  /**
   * Retrieves the issued date from the provided JWT token.
   *
   * @param token the JWT token
   * @return the issued date of the token
   */
  public Date getIssuedDateFromToken(final String token) {
    return getClaimFromToken(token, Claims::getIssuedAt);
  }

  /**
   * Validates the provided WAM Automation token and retrieves user details from it.
   *
   * @param token the JWT token
   * @return a WAMAutomationUserDetailsDto object containing user details
   * @throws ResponseStatusException if the token is expired
   */
  public WAMAutomationUserDetailsDto validateWAMAutomationToken(final String token) {
    if (isTokenExpired(token)) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "error.auth.access.token.expired");
    }
    final Claims claims = getAllClaimsFromToken(token);
    final WAMAutomationUserDetailsDto detailsDto =
        WAMAutomationUserDetailsDto.builder()
            .permissionTypeList((List<String>) claims.get(PERMISSION_TYPE_LIST))
            .isSuperAdmin((Boolean) claims.get(IS_SUPER_ADMIN))
            .firstName(String.valueOf(claims.get(FIRST_NAME)))
            .lastName(String.valueOf(claims.get(LAST_NAME)))
            .userEmail(String.valueOf(claims.get(USER_EMAIL)))
            .userId(String.valueOf(claims.get(USER_ID)))
            .token(token)
            .build();
    return detailsDto;
  }

  /**
   * Retrieves a specific claim from the provided JWT token using the specified resolver function.
   *
   * @param token the JWT token
   * @param claimsResolver a function that resolves the required claim
   * @param <T> the type of the claim
   * @return the resolved claim
   */
  public <T> T getClaimFromToken(final String token, final Function<Claims, T> claimsResolver) {
    final Claims claims = getAllClaimsFromToken(token);
    return claimsResolver.apply(claims);
  }

  /**
   * Retrieves all claims from the provided JWT token.
   *
   * @param token the JWT token
   * @return a Claims object containing all claims
   */
  private Claims getAllClaimsFromToken(final String token) {
    return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
  }

  /**
   * Validates whether the provided JWT token is expired.
   *
   * @param token the JWT token
   * @return true if the token is expired; false otherwise
   */
  private boolean isTokenExpired(final String token) {
    final Date expiration = getExpirationDateFromToken(token);
    return expiration.before(new Date());
  }

  /**
   * Generates a new JWT token based on the provided details.
   *
   * @param WAMAutomationTokenDto contains the details needed to create the token
   * @return a JWTTokenDto containing the generated token and its expiration date
   */
  public JWTTokenDto generateToken(final WAMAutomationTokenDto WAMAutomationTokenDto) {
    Map<String, Object> claims = new HashMap<>();
    claims.put(IS_SUPER_ADMIN, WAMAutomationTokenDto.isSuperAdmin());
    claims.put(PERMISSION_TYPE_LIST, WAMAutomationTokenDto.getPermissionTypeList());
    claims.put(FIRST_NAME, WAMAutomationTokenDto.getFirstName());
    claims.put(AppConstant.ClaimName.LAST_NAME, WAMAutomationTokenDto.getLastName());
    claims.put(AppConstant.ClaimName.USER_EMAIL, WAMAutomationTokenDto.getUserEmail());
    claims.put(AppConstant.ClaimName.USER_ID, WAMAutomationTokenDto.getUserId());
    claims.put(AppConstant.WAM_AUTOMATION_TOKEN_TTL, AppConstant.WAM_AUTOMATION_TOKEN_EXPIRATION);
    return doGenerateToken(
        claims, AppConstant.WAM_AUTOMATION_TOKEN_EXPIRATION, WAMAutomationTokenDto.getUserEmail());
  }

  /**
   * Generates the JWT token with the specified claims, expiration, and subject.
   *
   * @param claims the claims to be included in the token
   * @param currentExpireIn the duration in seconds until the token expires
   * @param subject the subject for the token
   * @return a JWTTokenDto containing the generated token and its expiration date
   */
  private JWTTokenDto doGenerateToken(
      final Map<String, Object> claims, final int currentExpireIn, final String subject) {
    long millis = TimeUnit.SECONDS.toMillis(currentExpireIn);
    Date expiration = new Date(System.currentTimeMillis() + millis);
    claims.put(AppConstant.WAM_AUTOMATION_TOKEN_TTL, expiration);
    final String token =
        Jwts.builder()
            .setClaims(claims)
            .setSubject(subject)
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(expiration)
            .signWith(SignatureAlgorithm.HS512, secret)
            .compact();
    return JWTTokenDto.builder().token(token).expiration(expiration).build();
  }

  /**
   * Generates a new JWT token with a different TTL from a valid token.
   *
   * @param token the original JWT token
   * @return a JWTTokenDto containing the new token and its expiration date
   * @throws ResponseStatusException if the original token is invalid or expired
   */
  public JWTTokenDto regenerateTokenWithNewTTL(final String token) {
    if (isTokenExpired(token)) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "error.auth.access.token.expired");
    }
    final Claims claims = getAllClaimsFromToken(token);
    final String subject = claims.getSubject();
    return doGenerateToken(
        new HashMap<>(claims), WAM_AUTOMATION_EXECUTION_TOKEN_EXPIRATION, subject);
  }
}
