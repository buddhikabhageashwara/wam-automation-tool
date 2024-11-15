package wam.automationtool.application.impl.alias;

import wam.automationtool.application.dto.alias.AliasParameterTypeAddRequestDto;
import wam.automationtool.application.dto.alias.AliasParameterTypeResponseDto;
import wam.automationtool.application.dto.alias.AliasParameterTypeUpdateRequestDto;
import wam.automationtool.application.dto.alias.AliasParameterTypesResponseDto;

public interface AliasParameterTypeService {

  void addAliasParameterType(
      AliasParameterTypeAddRequestDto aliasParameterTypeAddRequestDto);

  AliasParameterTypesResponseDto getAliasParameterTypes();
  AliasParameterTypeResponseDto getAliasParameterType(long aliasParameterTypeId);

  void updateAliasParameterType(
      long aliasParameterTypeId,
      AliasParameterTypeUpdateRequestDto aliasParameterTypeUpdateRequestDto);

  void deleteAliasParameterType(long aliasParameterTypeId);
}
