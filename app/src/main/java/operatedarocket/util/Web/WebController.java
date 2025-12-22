package operatedarocket.util.Web;

public class WebController {
    private String innerHTML;

    public WebController(String innerHTML) {
        this.innerHTML = innerHTML;
    }

    public String getInnerHTML() {return innerHTML;}

    public void setInnerHTML(String innerHTML) {this.innerHTML = innerHTML;}
}
