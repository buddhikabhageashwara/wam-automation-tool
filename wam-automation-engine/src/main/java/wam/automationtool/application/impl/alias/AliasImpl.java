package wam.automationtool.application.impl.alias;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static wam.automationtool.application.config.AppConstant.AuthConstants.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import wam.automationtool.application.dto.alias.AliasAddRequestDto;
import wam.automationtool.application.dto.alias.AliasDto;
import wam.automationtool.application.dto.alias.AliasListResponseDto;
import wam.automationtool.application.dto.alias.AliasResponseDto;
import wam.automationtool.application.exception.*;
import wam.automationtool.application.transform.AliasParameterTransformer;
import wam.automationtool.application.transform.AliasTransformer;
import wam.automationtool.domain.entity.testcasestep.alias.Alias;
import wam.automationtool.domain.entity.testcasestep.alias.AliasType;
import wam.automationtool.domain.entity.testcasestep.alias.parameter.AliasParameter;
import wam.automationtool.domain.entity.testcasestep.alias.parameter.AliasParameterType;
import wam.automationtool.domain.service.AliasDomainService;
import wam.automationtool.domain.service.AliasParameterDomainService;
import wam.automationtool.domain.service.AliasParameterTypeDomainService;
import wam.automationtool.infrastructure.provider.AuthDetailsProvider;

@Service
@Slf4j
@RequiredArgsConstructor
public class AliasImpl extends AuthDetailsProvider implements AliasService {

  private final AliasDomainService aliasDomainService;
  private final AliasParameterDomainService aliasParameterDomainService;
  private final AliasParameterTypeDomainService aliasParameterTypeDomainService;
  private final AliasTransformer aliasTransformer;
  private final AliasParameterTransformer aliasParameterTransformer;

  @Override
  @Transactional
  public void addAlias(final AliasAddRequestDto aliasAddRequestDto) {
    checkValidityOfAliasType(aliasAddRequestDto);
    checkAliasExistent(aliasAddRequestDto.getAliasName());
    final Alias newAlias = prepareAndAddAlias(aliasAddRequestDto);
    addAliasParameters(aliasAddRequestDto.getAliasParameters(), newAlias);
  }

  private Alias prepareAndAddAlias(final AliasAddRequestDto aliasAddRequestDto) {
    final Alias preparedAlias = aliasTransformer.aliasAddRequestDtoToAlias(aliasAddRequestDto);
    final Alias newAlias = aliasDomainService.add(preparedAlias);
    return newAlias;
  }

  private void addAliasParameters(
      final LinkedHashMap<String, String> aliasParameters, final Alias newAlias) {
    final List<AliasParameterType> aliasParameterTypeList = getAliasParameterTypes(aliasParameters);
    final List<AliasParameter> aliasParameterList =
        aliasParameterTransformer.aliasParameterMapToAliasParameterList(
            aliasParameters, newAlias, aliasParameterTypeList);
    aliasParameterDomainService.addAll(aliasParameterList);
  }

  private void checkAliasExistent(final String aliasName) {
    boolean isAliasExist = aliasDomainService.findByAliasName(aliasName).isPresent();
    if (isAliasExist) {
      throw new AliasAlreadyExistException(
          BAD_REQUEST, ALIAS_ALREADY_EXIST_CODE, "error.alias.already.exist");
    }
  }

  private void checkValidityOfAliasType(final AliasAddRequestDto aliasAddRequestDto) {
    final boolean isAliasTypeNotExist = AliasType.isNotExist(aliasAddRequestDto.getAliasType());
    if (isAliasTypeNotExist) {
      throw new InvalidAliasTypeException(
          BAD_REQUEST, INVALID_ALIAS_TYPE_CODE, "error.invalid.alias.type");
    }
  }

  private List<AliasParameterType> getAliasParameterTypes(
      final LinkedHashMap<String, String> aliasParameters) {
    return aliasParameters.entrySet().stream()
        .map(
            entry -> {
              final Long typeId = Long.valueOf(entry.getKey());
              AliasParameterType preferenceParameterType =
                  aliasParameterTypeDomainService
                      .findById(typeId)
                      .orElseThrow(
                          () ->
                              new AliasParameterTypeNotFoundException(
                                  NOT_FOUND,
                                  ALIAS_PARAMETER_TYPE_NOT_FOUND_CODE,
                                  "error.alias.parameter.type.not.found"));
              return preferenceParameterType;
            })
        .collect(Collectors.toList());
  }

  @Override
  public AliasListResponseDto getAliasList() {
    final List<Alias> aliasList = aliasDomainService.findAll();
    final List<AliasDto> aliasDtoList = aliasTransformer.aliasListToAliasDtoList(aliasList);
    return AliasListResponseDto.builder().aliasDtoList(aliasDtoList).build();
  }

  @Override
  public AliasResponseDto getAlias(final long aliasId) {
    final Alias alias =
        aliasDomainService
            .findById(aliasId)
            .orElseThrow(
                () ->
                    new AliasNotFoundException(
                        NOT_FOUND,
                        ALIAS_NOT_FOUND_CODE,
                        "error.alias.not.found"));
    final AliasDto aliasDto = aliasTransformer.aliasToAliasDto(alias);
    return AliasResponseDto.builder().aliasDto(aliasDto).build();
  }

  @Override
  public void deleteAlias(final long aliasId) {
    final Alias alias =
            aliasDomainService
                    .findById(aliasId)
                    .orElseThrow(
                            () ->
                                    new AliasNotFoundException(
                                            NOT_FOUND,
                                            ALIAS_NOT_FOUND_CODE,
                                            "error.alias.not.found"));
    aliasDomainService.delete(alias);
  }
}
