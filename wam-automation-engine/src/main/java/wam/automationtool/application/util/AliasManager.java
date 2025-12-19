package wam.automationtool.application.util;


import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import wam.automationtool.application.dto.alias.AliasDto;

public class AliasManager {

  public static List<String> extractAlias(final String value, final List<AliasDto> aliasDtoList) {
    List<String> extractedAliasList = new ArrayList<>();
    final Pattern pattern = Pattern.compile("\\[\\[\\[(.*?)]]]");
    final Matcher matcher = pattern.matcher(value);
    while (matcher.find()) {
      extractedAliasList.add(matcher.group(1));
    }
    return extractedAliasList;
  }
}
