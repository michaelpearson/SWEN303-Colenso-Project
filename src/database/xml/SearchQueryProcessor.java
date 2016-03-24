package database.xml;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchQueryProcessor {
    private static Pattern tokenize = Pattern.compile("(?:\"([^\"]+)\"|([\\w*?'\\.]+)|(\\()|(\\)))");

    public static String processQuery(String query) {

        ArrayList<String> tokens = new ArrayList<>();

        Matcher matcher = tokenize.matcher(query);
        while(matcher.find()) {
            tokens.add(matcher.group(1) == null ? matcher.group() : matcher.group(1));
        }


        StringBuilder builtQuery = new StringBuilder();
        boolean needsComma = false;
        for(int a = 0;a < tokens.size();a++) {
            String token = tokens.get(a);
            if(keywordType(token) == 2) {
                builtQuery.append(token);
                needsComma = false;
                continue;
            }
            if(a > 0 && keywordType(token) == 1 && (keywordType(tokens.get(a - 1)) == 0)) {
                if(token.equals("not") && a > 0 && !tokens.get(a - 1).equals("and")) {
                    token = "and ftnot";
                }
                builtQuery.append(" ft" + token + " ");
                needsComma = false;
                continue;
            }

            if(!needsComma) {
                builtQuery.append(" {'" + token + "'");
            } else {
                builtQuery.append(", '" + token + "'");
            }
            if(a < tokens.size() - 1 && keywordType(tokens.get(a + 1)) == 0) {
                needsComma = true;
            } else {
                needsComma = false;
                builtQuery.append("} using wildcards ");
            }
        }

        return builtQuery.toString();
    }

    private static int keywordType(String token) {
        if(token.equals("and") || token.equals("not") || token.equals("or")) {
            return 1;
        }
        if(token.equals("(") || token.equals(")")) {
            return 2;
        }
        return 0;
    }
}
