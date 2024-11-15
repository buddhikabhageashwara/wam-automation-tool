package wam.automationtool.application.impl.alias;

import wam.automationtool.application.dto.alias.AliasAddRequestDto;
import wam.automationtool.application.dto.alias.AliasResponseDto;
import wam.automationtool.application.dto.alias.AliasListResponseDto;

public interface AliasService {

  void addAlias(
      AliasAddRequestDto aliasAddRequestDto);

  AliasListResponseDto getAliasList();
  AliasResponseDto getAlias(long aliasId);

  void deleteAlias(long aliasId);
}
