package carpet.script;

import carpet.script.exception.ExpressionException;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * sole purpose of this package is to provide public access to package-private methods of Expression and CarpetExpression
 * classes so they don't leave garbage in javadocs
 */
public class ExpressionInspector
{
    public static final String MARKER_STRING = "__scarpet_marker";
    public static List<String> Expression_getExpressionSnippet(Tokenizer.Token token, Expression expr)
    {
        return Expression.getExpressionSnippet(token, expr);
    }

    public static String Expression_getName(Expression e)
    {
        return e.getName();
    }

    public static Expression Expression_cloneWithName(Expression e, String name, Tokenizer.Token token)
    {
        Expression copy = null;
        try
        {
            copy = e.clone();
        }
        catch (CloneNotSupportedException ex)
        {
            throw new ExpressionException(e, token, "Problems in allocating global function "+name);
        }
        return copy.withName(name);
    }

    private static TreeSet<String> scarpetFunctions;
    private static TreeSet<String> APIFunctions;
    static
    {
        Set<String> allFunctions = (new CarpetExpression("null", null, null)).getExpr().getFunctionNames();
        scarpetFunctions = new TreeSet<>(new Expression("null").getFunctionNames());
        APIFunctions = new TreeSet<>(allFunctions.stream().filter(s -> !scarpetFunctions.contains(s)).collect(Collectors.toSet()));
    }

    public static List<String> suggestFunctions(ScriptHost host, String previous, String prefix)
    {
        previous = previous.replace("\\'", "");
        int quoteCount = StringUtils.countMatches(previous,'\'');
        if (quoteCount % 2 == 1)
            return Collections.emptyList();
        int maxLen = prefix.length()<3 ? (prefix.length()*2+1) : 1234;
        List<String> scarpetMatches = scarpetFunctions.stream().
                filter(s -> s.startsWith(prefix) && s.length() <= maxLen).map(s -> s+"(").collect(Collectors.toList());
        scarpetMatches.addAll(APIFunctions.stream().
                filter(s -> s.startsWith(prefix) && s.length() <= maxLen).map(s -> s+"(").collect(Collectors.toList()));
        // not that useful in commandline, more so in external scripts, so skipping here
        //scarpetMatches.addAll(CarpetServer.scriptServer.events.eventHandlers.keySet().stream().
        //        filter(s -> s.startsWith(prefix) && s.length() <= maxLen).map(s -> "__"+s+"(").collect(Collectors.toList()));
        scarpetMatches.addAll(host.globalFunctions.keySet().stream().
                filter(s -> s.startsWith(prefix)).map(s -> s+"(").collect(Collectors.toList()));
        scarpetMatches.addAll(host.globalVariables.keySet().stream().
                filter(s -> s.startsWith(prefix)).collect(Collectors.toList()));
        return scarpetMatches;
    }
}
