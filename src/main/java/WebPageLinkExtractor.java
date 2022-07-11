import dto.Page;
import lombok.AllArgsConstructor;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveAction;

@AllArgsConstructor
public class WebPageLinkExtractor extends RecursiveAction {

    private PageParser parser;

    @Override
    protected void compute() {
        parser.parsePageAndGetSubPages();
        List<WebPageLinkExtractor> taskList = new ArrayList<>();
        for (String childPages : parser.getChildrenPages()) {
            WebPageLinkExtractor task = new WebPageLinkExtractor(new PageParser(childPages));
            task.fork();
            taskList.add(task);
        }
        for (WebPageLinkExtractor task : taskList) {
            task.join();
        }
    }
}
