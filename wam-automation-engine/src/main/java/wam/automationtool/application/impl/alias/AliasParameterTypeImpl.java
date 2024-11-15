package wam.automationtool.application.impl.alias;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static wam.automationtool.application.config.AppConstant.AuthConstants.ALIAS_PARAMETER_TYPE_ALREADY_EXIST_CODE;
import static wam.automationtool.application.config.AppConstant.AuthConstants.ALIAS_PARAMETER_TYPE_NOT_FOUND_CODE;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import wam.automationtool.application.dto.alias.AliasParameterTypeAddRequestDto;
import wam.automationtool.application.dto.alias.AliasParameterTypeDto;
import wam.automationtool.application.dto.alias.AliasParameterTypeResponseDto;
import wam.automationtool.application.dto.alias.AliasParameterTypeUpdateRequestDto;
import wam.automationtool.application.dto.alias.AliasParameterTypesResponseDto;
import wam.automationtool.application.exception.AliasParameterTypeAlreadyExistException;
import wam.automationtool.application.exception.AliasParameterTypeNotFoundException;
import wam.automationtool.application.transform.AliasParameterTypeTransformer;
import wam.automationtool.domain.entity.testcasestep.alias.parameter.AliasParameterType;
import wam.automationtool.domain.service.AliasParameterTypeDomainService;
import wam.automationtool.infrastructure.provider.AuthDetailsProvider;

@Service
@Slf4j
@RequiredArgsConstructor
public class AliasParameterTypeImpl extends AuthDetailsProvider
    implements AliasParameterTypeService {

  private final AliasParameterTypeDomainService aliasParameterTypeDomainService;
  private final AliasParameterTypeTransformer aliasParameterTypeTransformer;

  @Override
  public void addAliasParameterType(
      final AliasParameterTypeAddRequestDto aliasParameterTypeAddRequestDto) {
    boolean isAliasParameterTypeExist =
        aliasParameterTypeDomainService
            .findByParameterName(aliasParameterTypeAddRequestDto.getParameterName())
            .isPresent();
    if (isAliasParameterTypeExist) {
      throw new AliasParameterTypeAlreadyExistException(
          BAD_REQUEST,
          ALIAS_PARAMETER_TYPE_ALREADY_EXIST_CODE,
          "error.alias.parameter.type.already.exist");
    }
    final AliasParameterType aliasParameterType =
        aliasParameterTypeTransformer
            .aliasParameterTypeAddRequestDtoToAliasParameterType(
                aliasParameterTypeAddRequestDto);
    aliasParameterTypeDomainService.add(aliasParameterType);
  }

  @Override
  public AliasParameterTypesResponseDto getAliasParameterTypes() {
    final List<AliasParameterType> aliasParameterTypeList =
        aliasParameterTypeDomainService.findAll();
    final List<AliasParameterTypeDto> aliasParameterTypeDtoList =
        aliasParameterTypeTransformer.aliasParameterTypeListToDtoList(aliasParameterTypeList);
    return AliasParameterTypesResponseDto.builder()
        .aliasParameterTypeDtoList(aliasParameterTypeDtoList)
        .build();
  }

  @Override
  public AliasParameterTypeResponseDto getAliasParameterType(
      final long aliasParameterTypeId) {
    final AliasParameterType aliasParameterType =
        aliasParameterTypeDomainService
            .findById(aliasParameterTypeId)
            .orElseThrow(
                () ->
                    new AliasParameterTypeNotFoundException(
                        NOT_FOUND,
                        ALIAS_PARAMETER_TYPE_NOT_FOUND_CODE,
                        "error.alias.parameter.type.not.found"));
    final AliasParameterTypeDto aliasParameterTypeDto =
        aliasParameterTypeTransformer.aliasParameterTypeToDto(aliasParameterType);
    return AliasParameterTypeResponseDto.builder()
        .aliasParameterTypeDto(aliasParameterTypeDto)
        .build();
  }

  @Override
  public void updateAliasParameterType(
      final long aliasParameterTypeId,
      final AliasParameterTypeUpdateRequestDto aliasParameterTypeUpdateRequestDto) {
    aliasParameterTypeDomainService
                    .findById(aliasParameterTypeId)
                    .orElseThrow(
                            () ->
                                    new AliasParameterTypeNotFoundException(
                                            NOT_FOUND,
                                            ALIAS_PARAMETER_TYPE_NOT_FOUND_CODE,
                                            "error.alias.parameter.type.not.found"));
    final AliasParameterType aliasParameterType =
            aliasParameterTypeTransformer.aliasParameterTypeUpdateRequestDtoToAliasParameterType(
            aliasParameterTypeUpdateRequestDto);
    aliasParameterTypeDomainService.update(aliasParameterType);
  }

  @Override
  public void deleteAliasParameterType(final long aliasParameterTypeId) {
    final AliasParameterType existingAliasParameterType =
            aliasParameterTypeDomainService
                    .findById(aliasParameterTypeId)
                    .orElseThrow(
                            () ->
                                    new AliasParameterTypeNotFoundException(
                                            NOT_FOUND,
                                            ALIAS_PARAMETER_TYPE_NOT_FOUND_CODE,
                                            "error.alias.parameter.type.not.found"));
    aliasParameterTypeDomainService.delete(existingAliasParameterType);
  }
}
