package wam.automationtool.application.impl.parameter;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static wam.automationtool.application.config.AppConstant.AuthConstants.PREFERENCE_PARAMETER_TYPE_ALREADY_EXIST_CODE;
import static wam.automationtool.application.config.AppConstant.AuthConstants.PREFERENCE_PARAMETER_TYPE_NOT_FOUND_CODE;

import java.util.List;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import wam.automationtool.application.dto.parameter.PreferenceParameterTypeAddRequestDto;
import wam.automationtool.application.dto.parameter.PreferenceParameterTypeDto;
import wam.automationtool.application.dto.parameter.PreferenceParameterTypeResponseDto;
import wam.automationtool.application.dto.parameter.PreferenceParameterTypeUpdateRequestDto;
import wam.automationtool.application.dto.parameter.PreferenceParameterTypesResponseDto;
import wam.automationtool.application.exception.PreferenceParameterTypeAlreadyExistException;
import wam.automationtool.application.exception.PreferenceParameterTypeNotFoundException;
import wam.automationtool.application.transform.PreferenceParameterTypeTransformer;
import wam.automationtool.domain.entity.testcasestep.parameter.PreferenceParameterType;
import wam.automationtool.domain.service.PreferenceParameterTypeDomainService;
import wam.automationtool.infrastructure.provider.AuthDetailsProvider;

@Service
@Slf4j
@RequiredArgsConstructor
public class PreferenceParameterTypeImpl extends AuthDetailsProvider
    implements PreferenceParameterTypeService {

  private final PreferenceParameterTypeDomainService preferenceParameterTypeDomainService;
  private final PreferenceParameterTypeTransformer preferenceParameterTypeTransformer;

  @Override
  @Transactional
  public void addPreferenceParameterType(
      final PreferenceParameterTypeAddRequestDto preferenceParameterTypeAddRequestDto) {
    boolean isPreferenceParameterTypeExist =
        preferenceParameterTypeDomainService
            .findByParameterName(preferenceParameterTypeAddRequestDto.getParameterName())
            .isPresent();
    if (isPreferenceParameterTypeExist) {
      throw new PreferenceParameterTypeAlreadyExistException(
          BAD_REQUEST,
          PREFERENCE_PARAMETER_TYPE_ALREADY_EXIST_CODE,
          "error.preference.parameter.type.already.exist");
    }
    final PreferenceParameterType preferenceParameterType =
        preferenceParameterTypeTransformer
            .preferenceParameterTypeAddRequestDtoToPreferenceParameterType(
                preferenceParameterTypeAddRequestDto);
    preferenceParameterTypeDomainService.add(preferenceParameterType);
  }

  @Override
  public PreferenceParameterTypesResponseDto getPreferenceParameterTypes() {
    final List<PreferenceParameterType> preferenceParameterTypeList =
        preferenceParameterTypeDomainService.findAll();
    final List<PreferenceParameterTypeDto> preferenceParameterTypeDtoList =
        preferenceParameterTypeTransformer.preferenceParameterTypeListToDtoList(
            preferenceParameterTypeList);
    return PreferenceParameterTypesResponseDto.builder()
        .preferenceParameterTypeDtoList(preferenceParameterTypeDtoList)
        .build();
  }

  @Override
  public PreferenceParameterTypeResponseDto getPreferenceParameterType(
      final long preferenceParameterTypeId) {
    final PreferenceParameterType preferenceParameterType =
        preferenceParameterTypeDomainService
            .findById(preferenceParameterTypeId)
            .orElseThrow(
                () ->
                    new PreferenceParameterTypeNotFoundException(
                        NOT_FOUND,
                        PREFERENCE_PARAMETER_TYPE_NOT_FOUND_CODE,
                        "error.preference.parameter.type.not.found"));
    final PreferenceParameterTypeDto preferenceParameterTypeDto =
        preferenceParameterTypeTransformer.preferenceParameterTypeToDto(preferenceParameterType);
    return PreferenceParameterTypeResponseDto.builder()
        .preferenceParameterTypeDto(preferenceParameterTypeDto)
        .build();
  }

  @Override
  public void updatePreferenceParameterType(
      final long preferenceParameterTypeId,
      final PreferenceParameterTypeUpdateRequestDto preferenceParameterTypeUpdateRequestDto) {
    preferenceParameterTypeDomainService
                    .findById(preferenceParameterTypeId)
                    .orElseThrow(
                            () ->
                                    new PreferenceParameterTypeNotFoundException(
                                            NOT_FOUND,
                                            PREFERENCE_PARAMETER_TYPE_NOT_FOUND_CODE,
                                            "error.preference.parameter.type.not.found"));
    final PreferenceParameterType preferenceParameterType =
            preferenceParameterTypeTransformer.preferenceParameterTypeUpdateRequestDtoToPreferenceParameterType(
            preferenceParameterTypeUpdateRequestDto);
    preferenceParameterTypeDomainService.update(preferenceParameterType);
  }

  @Override
  public void deletePreferenceParameterType(final long preferenceParameterTypeId) {
    final PreferenceParameterType existingPreferenceParameterType =
            preferenceParameterTypeDomainService
                    .findById(preferenceParameterTypeId)
                    .orElseThrow(
                            () ->
                                    new PreferenceParameterTypeNotFoundException(
                                            NOT_FOUND,
                                            PREFERENCE_PARAMETER_TYPE_NOT_FOUND_CODE,
                                            "error.preference.parameter.type.not.found"));
    preferenceParameterTypeDomainService.delete(existingPreferenceParameterType);
  }
}
