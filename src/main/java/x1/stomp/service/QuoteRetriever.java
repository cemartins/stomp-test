package x1.stomp.service;

import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;

import x1.stomp.model.Quote;
import x1.stomp.model.Share;

public class QuoteRetriever {
	private static final String URL = "http://finance.yahoo.com/d/quotes.csv?s={0}&f=sna";
	private static final String CURRENCY = "EUR";

	/*
	static {
			Authenticator.setDefault(new ProxyAuthenticator("cem", "sporting"));
	}*/

	@Inject
	private Logger log;

	public Quote retrieveQuote(Share share) {
		try {
			String content = retrieveInternal(share.getKey());
			return createQuote(content, share);
		} catch (IOException e) {
			log.warn(null, e);
			return null;
		}
	}

	public List<Quote> retrieveQuotes(List<Share> shares) {
		StringBuffer buffer = new StringBuffer();
		for (Share share : shares) {
			if (buffer.length() > 0) {
				buffer.append('+');
			}
			buffer.append(share.getKey());
		}

		List<Quote> result = new ArrayList<>();
		try {
			String content = retrieveInternal(buffer.toString());
			StringTokenizer tokenizer = new StringTokenizer(content, "\n\r");
			while (tokenizer.hasMoreTokens()) {
				String line = tokenizer.nextToken();
				Quote quote = createQuote(line, shares);
				if (quote != null) {
					result.add(quote);
				}
			}
		} catch (IOException e) {
			log.warn(null, e);
		}
		return result;
	}

	private String retrieveInternal(String keys) throws IOException {
		log.debug("Retrieve quotes for " + keys);
		String targetUrl = MessageFormat.format(URL, keys.toUpperCase());
		HttpHost proxyHost = new HttpHost("proxy.adx.intra", 3128);
		String content = Request.Get(targetUrl).viaProxy(proxyHost).execute().returnContent()
				.asString();
		log.debug("Received content size:" + content.length());
		return content;
	}

	/*
	private String retrieveIntegrnalThroughProxy(String keys) throws IOException {
		
		log.debug("Retrieve quotes for " + keys);
		
		String targetUrl = MessageFormat.format("d/quotes.csv?s={0}&f=sna",	keys.toUpperCase());
		
		CloseableHttpResponse response = null;
		CloseableHttpClient httpclient = null;
		
		try {

			CredentialsProvider credsProvider = new BasicCredentialsProvider();
			credsProvider.setCredentials(
					new AuthScope(null, -1),
					new UsernamePasswordCredentials("cem", "sporting"));

			httpclient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
			HttpHost target = new HttpHost("de.finance.yahoo.com", 80, "http");
			HttpHost proxy = new HttpHost("proxy.adx.intra", 3128);

			RequestConfig config = RequestConfig.custom().setProxy(proxy)
					.build();
			HttpGet httpget = new HttpGet(targetUrl);
			httpget.setConfig(config);

			response = httpclient.execute(target, httpget);

			String content = EntityUtils.toString(response.getEntity());

			log.debug("Received content size:" + content.length());
			return content;

		} finally {
			if(response != null)
				response.close();
			if(httpclient != null)
				httpclient.close();
		}
	}
	*/

	private Quote createQuote(String line, List<Share> shares) {
		String[] parts = StringUtils.split(line, ',');
		if (parts.length != 3) {
			return null;
		}
		String key = StringUtils.remove(parts[0], "\"").trim();
		String name = StringUtils.remove(parts[1], "\"").trim();
		String price = parts[2].trim();
		if (price.equalsIgnoreCase("N/A")) {
			return null;
		}
		for (Share share : shares) {
			if (share.getKey().equalsIgnoreCase(key)) {
				share.setKey(key);
				share.setName(name);
				Quote quote = new Quote(share);
				quote.setPrice(Float.valueOf(price));
				quote.setCurrency(CURRENCY);
				return quote;
			}
		}
		return null;
	}

	private Quote createQuote(String line, Share share) {
		return createQuote(line, Arrays.asList(share));
	};
	
	private static class ProxyAuthenticator extends Authenticator {

		private final String userName;
		private final String password;

		public ProxyAuthenticator(String userName, String password) {
			this.userName = userName;
			this.password = password;
		}

		@Override
		protected PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication(userName, password.toCharArray());
		}
	}
}
