/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tmp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import junit.framework.TestCase;

/**
 *
 * @author strike
 */
public class RegexTest extends TestCase {

    public void testRegex() {
        String arg = "{11943}(163,1694)[731,937,1013,1694]";
        String strpattern = "\\{(\\d+)\\}\\((\\d+),(\\d+)\\)\\[([0-9,]+)\\]";
        Pattern pattern = Pattern.compile(strpattern);
        Matcher matcher = pattern.matcher(arg);
        if (matcher.matches()) {
//            System.out.println(matcher.group(0));
            System.out.println(matcher.group(1));
            System.out.println(matcher.group(2));
            System.out.println(matcher.group(3));
            System.out.println(matcher.group(4));
        }

    }
}
