package operatedarocket.Web.Controllers.Introduction;

import operatedarocket.util.HashMaps.HashMapBuilder;
import operatedarocket.util.Web.WebController;

public class IntroductionController extends WebController {
    StringBuilder info = new StringBuilder();

    public IntroductionController(String innerHTML) {
        super(innerHTML);

        info.append("<ul>");
        System.getProperties().forEach((key, value) -> {
            if (!key.toString().startsWith("j")) {
                info
                        .append("<li>")
                        .append(key.toString())
                        .append(" = ")
                        .append(value.toString())
                        .append("</li>\n");
            }
        });
        info.append("</ul>");

        new HashMapBuilder<String, String>()
                .put("{$1}", info.toString())
                .build().forEach((key, value) -> {
                    setInnerHTML(getInnerHTML().replace(key, value));
                });
    }
}
