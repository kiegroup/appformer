package org.guvnor.structure.backend;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.text.StringEscapeUtils;
import org.uberfire.security.Contributor;

public class InputEscapeUtils {

    public static Collection<Contributor> escapeContributorsNames(Collection<Contributor> contributors) {
        Collection<Contributor> escapedContributors = new ArrayList<>();
        contributors.forEach((contributor -> {
            String escapedName = escapeHtmlInput(contributor.getUsername());
            escapedContributors.add(new Contributor(escapedName, contributor.getType()));
        }));
        return escapedContributors;
    }

    public static String escapeHtmlInput(String input) {
        if (input != null) {
            String escapedInput = StringEscapeUtils.escapeHtml4(input);
            escapedInput = escapedInput.replace("'", "");
            return escapedInput;
        } else {
            return null;
        }
    }

}
