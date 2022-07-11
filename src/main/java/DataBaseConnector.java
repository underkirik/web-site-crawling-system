import dto.Page;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import java.util.List;

public class DataBaseConnector {

    private static volatile SessionFactory sessionFactory;

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null){
            synchronized (DataBaseConnector.class){
                if (sessionFactory == null){
                    StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                            .configure("hibernate.cfg.xml").build();
                    Metadata metadata = new MetadataSources(registry).getMetadataBuilder().build();
                    sessionFactory = metadata.getSessionFactoryBuilder().build();
                }
            }
        }
        return sessionFactory;
    }

    public static void closeSessionFactory(Session session) {
        session.flush();
        session.close();
        session.getSessionFactory().close();
    }

}
