import dto.Page;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
public class PageParser {

    private static volatile Logger logger;
    private final String address;
    private static volatile String parentPageAddress;
    private Document document;
    private final Set<String> childrenPages;
    private Page page;

    public PageParser(String address) {
        if (logger == null) {
            synchronized (PageParser.class) {
                if (logger == null) {
                    logger = LogManager.getLogger(PageParser.class);
                }
            }
        }
        childrenPages = new HashSet<>();
        this.address = address;
        document = getDocument();
    }

    private Document getDocument() {
        if (document == null) {
            try {
                document = Jsoup.connect(address)
                        .userAgent("UnderkirikSearchBot")
                        .referrer("http://www.google.com")
                        .get();
                logger.info("Connect to " + address);
            } catch (IOException e) {
                logger.warn("Page: " + address + "is not Found or error fetching");
            }
        }
        return document;
    }

    public void parsePageAndGetSubPages() {
        if (existsPageByPath(address)){
            return;
        }
        page = new Page();
        page.setPath(address);
        try {
            page.setCode(getDocument().connection().execute().statusCode());
        } catch (IOException e) {
            logger.error("Page: " + address + "is not found or error fetching");
        }
        page.setContent(document.html());
        saveInDb(page);
        logger.info("Write in db: " + page);
        if (parentPageAddress == null) {
            synchronized (PageParser.class){
                if (parentPageAddress == null){
                    parentPageAddress = address;
                }
            }
        }
        Elements links = document.select("a[href]");
        for (Element link : links) {
            String addressPage = link.attr("abs:href");
            if (addressPage.startsWith(parentPageAddress)
                    && !containsInvalidSymbols(addressPage)) {
                childrenPages.add(addressPage);
            }
        }
    }

    private boolean existsPageByPath(String address) {
        Session session = DataBaseConnector.getSessionFactory().openSession();
        String hqlToGetConnectedList = "FROM " + Page.class.getSimpleName() + " WHERE " + "path = '" + address + "'";
        List<Page> connectedList = session.createQuery(hqlToGetConnectedList, Page.class).getResultList();
        return !connectedList.isEmpty();
    }

    private void saveInDb(Page page) {
        Session session = DataBaseConnector.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        session.persist(page);
        transaction.commit();
    }

    private boolean containsInvalidSymbols(String address) {
        final String[] invalidSymbols = new String[]{"#", "?", "=", ".pdf", ".docx", ".pnq", ".png", ".jpg", ".jpeg"};
        for (String symbol : invalidSymbols) {
            if (address.contains(symbol)) {
                return true;
            }
        }
        return false;
    }
}
