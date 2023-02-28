package au.com.cybersearch2.taq.log;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Properties;
import java.io.IOException;

import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import org.xmlpull.v1.XmlPullParserException;

public class XmlConfigurationTest {

/*
km2_to_mi2(km2=1323.98, mi2=511.188678)
term q=one+two (1,1) (1,18)
.level = SEVERE
com.xyz.foo.handlers = java.util.logging.FileHandler
com.xyz.foo.level = SEVERE
com.xyz.foo.useparenthandlers = true
handlers = java.util.logging.ConsoleHandler
java.util.logging.ConsoleHandler.encoding = UTF-8
java.util.logging.ConsoleHandler.filter = MyFilter
java.util.logging.ConsoleHandler.formatter = java.util.logging.SimpleFormatter
java.util.logging.ConsoleHandler.level = INFO
java.util.logging.FileHandler.append = true
java.util.logging.FileHandler.count = 10
java.util.logging.FileHandler.encoding = UTF-8
java.util.logging.FileHandler.filter = MyFilter
java.util.logging.FileHandler.formatter = java.util.logging.XmlFormatter
java.util.logging.FileHandler.level = WARNING
java.util.logging.FileHandler.limit = 50000
java.util.logging.FileHandler.maxlocks = 100
java.util.logging.FileHandler.pattern = %h/java%u.log
java.util.logging.SimpleFormatter.format = %4$s: %5$s [%1$tc]%n	 */
	
	static String[] TEST_DOC = {
			
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
			"<Configuration>",
			"  <Properties>",
			"    <Property name=\"java.util.logging.SimpleFormatter.format\">%4$s: %5$s [%1$tc]%n</Property>",
			"  </Properties>",
			"  <Appenders>",
			"    <Console>",
			"      <Level>INFO</Level>",
			"      <Filter>MyFilter</Filter>",
			"      <Layout>java.util.logging.SimpleFormatter</Layout>",
			"      <Encoding>UTF-8</Encoding>",
			"    </Console>",
			"    <File>",
			"      <Level>WARNING</Level>",
			"      <Filter>MyFilter</Filter>",
			"      <Layout>java.util.logging.XmlFormatter</Layout>",
			"      <Encoding>UTF-8</Encoding>",
			"      <Count>10</Count>",
			"      <Limit>50000</Limit>",
			"      <Pattern>%h/java%u.log</Pattern>",
			"      <Append>true</Append>",
			"      <MaxLocks>100</MaxLocks>",
			"    </File>",
			"  </Appenders>",
			"  <Loggers>",
			"    <Logger name=\"com.xyz.foo\">",
			"      <Level>SEVERE</Level>",
			"      <UseParentHandlers>true</UseParentHandlers>",
			"      <AppenderRef ref=\"File\"/>",
			"    </Logger>",
			"    <Root level=\"SEVERE\">",
			"      <AppenderRef ref=\"Console\"/>",
			"    </Root>",
			"  </Loggers>",
			"</Configuration>",
	};
	
	@Test
	public void testXmlConfiguration() {
		StringBuilder builder = new StringBuilder();
		Arrays.asList(TEST_DOC).forEach(line -> builder.append(line).append('\n'));
		XmlConfiguration xmlConfiguration;
		Properties properties = null;
		try {
			xmlConfiguration = new XmlConfiguration();
		    InputStream is = new ByteArrayInputStream(builder.toString().getBytes(StandardCharsets.UTF_8));
		    properties = xmlConfiguration.parseXmlConfiguration(is);
		} catch (XmlPullParserException | IOException e) {
			e.printStackTrace();
			fail();
		}
		assertThat(properties).isNotNull();
		//TreeMap<Object,Object> map = new TreeMap<>();
		//map.putAll(properties);
		//map.forEach((key,value) -> System.out.println(String.format("%s = %s", key.toString(), value.toString())));
	}
}
