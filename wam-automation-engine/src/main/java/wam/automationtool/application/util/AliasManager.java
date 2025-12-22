package wam.automationtool.application.util;


import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import wam.automationtool.application.dto.alias.AliasDto;

public class AliasManager {

    public static AliasDto extractAlias(final String value, final List<AliasDto> aliasDtoList) {
        return aliasDtoList.stream()
                .filter(aliasDto -> value.equals(aliasDto.getAliasName()))
                .findFirst()
                .orElse(null);
    }
}
