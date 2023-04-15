package gu.dtalk;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * version information for project com.gitee.l0km:dtalk-base
 * @author guyadong
 */
public final class Version {
    /** project version */
    public static final String VERSION;
    static {
    	try(InputStream is = Version.class.getResourceAsStream("/version.properties")){
    		Properties properties=new Properties();
    		properties.load(is);
    		VERSION=properties.getProperty("VERSION");
    	} catch (IOException e) {
			throw new ExceptionInInitializerError(e);
		}
    }
}