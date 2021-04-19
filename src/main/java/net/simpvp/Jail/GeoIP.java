package net.simpvp.Jail;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.maxmind.geoip.LookupService;
import com.maxmind.geoip.Country;

public class GeoIP {

	private static LookupService asn4 = null;
	private static LookupService asn6 = null;
	private static LookupService country4 = null;
	private static LookupService country6 = null;

	public static void init() {
		String geoipDatabaseDir = Jail.instance.getConfig().getString("geoipDatabase");

		if (geoipDatabaseDir == null) {
			Jail.instance.getLogger().info("Not using geoip database");
			return;
		}

		if (!geoipDatabaseDir.endsWith("/")) {
			geoipDatabaseDir += "/";
		}

		try {
			asn4 = new LookupService(geoipDatabaseDir + "GeoIPASNum.dat", LookupService.GEOIP_MEMORY_CACHE | LookupService.GEOIP_CHECK_CACHE);
			asn6 = new LookupService(geoipDatabaseDir + "GeoIPASNumv6.dat", LookupService.GEOIP_MEMORY_CACHE | LookupService.GEOIP_CHECK_CACHE);
			country4 = new LookupService(geoipDatabaseDir + "GeoIP.dat", LookupService.GEOIP_MEMORY_CACHE | LookupService.GEOIP_CHECK_CACHE);
			country6 = new LookupService(geoipDatabaseDir + "GeoIPv6.dat", LookupService.GEOIP_MEMORY_CACHE | LookupService.GEOIP_CHECK_CACHE);

			Jail.instance.getLogger().info("Initiated geoip database");
		} catch (Exception e) {
			Jail.instance.getLogger().severe("Error instantiating GeoIP database " + e);
			e.printStackTrace();
		}
	}

	public static void close() {
		if (asn4 != null) {
			asn4.close();
			asn4 = null;
		}
		if (asn6 != null) {
			asn6.close();
			asn6 = null;
		}
		if (country4 != null) {
			country4.close();
			country4 = null;
		}
		if (country6 != null) {
			country6.close();
			country6 = null;
		}
	}

	private static Pattern ASN_REGEX = Pattern.compile("^AS([0-9]+).*");
	public static Integer getAsn(InetAddress ip) {
		if (asn4 == null || asn6 == null) {
			return null;
		}

		String asn = null;
		if (ip instanceof Inet4Address) {
			asn = asn4.getOrg(ip);
		} else if (ip instanceof Inet6Address) {
			asn = asn6.getOrgV6(ip);
		}
		if (asn == null) {
			return null;
		}

		Matcher m = ASN_REGEX.matcher(asn);
		if (!m.find()) {
			Jail.instance.getLogger().info("Jail ASN regex did not match: " + asn);
		}
		String match = m.group(1);
		return Integer.valueOf(match);
	}

	public static String getCountry(InetAddress ip) {
		if (country4 == null || country6 == null) {
			return null;
		}

		Country country = null;
		if (ip instanceof Inet4Address) {
			country = country4.getCountry(ip);
		} else if (ip instanceof Inet6Address) {
			country = country6.getCountryV6(ip);
		}
		if (country == null) {
			return null;
		}

		return country.getCode();
	}
}
