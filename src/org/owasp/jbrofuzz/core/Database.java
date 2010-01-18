/**
 * JBroFuzz 1.9
 *
 * JBroFuzz - A stateless network protocol fuzzer for web applications.
 * 
 * Copyright (C) 2007 - 2010 subere@uncon.org
 *
 * This file is part of JBroFuzz.
 * 
 * JBroFuzz is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * JBroFuzz is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with JBroFuzz.  If not, see <http://www.gnu.org/licenses/>.
 * Alternatively, write to the Free Software Foundation, Inc., 51 
 * Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * 
 * Verbatim copying and distribution of this entire program file is 
 * permitted in any medium without royalty provided this notice 
 * is preserved. 
 * 
 */
package org.owasp.jbrofuzz.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.CharUtils;
import org.apache.commons.lang.StringUtils;

/**
 * <p>
 * A database is a collection of prototypes, as loaded from the internal file
 * <code>fuzzers.jbrofuzz</code> residing within the JBroFuzz.jar file.
 * </p>
 * 
 * <p>
 * Once a database instance has been created, you can obtain any known fuzzer
 * through the factory method {@link #createFuzzer(String, int)}.
 * </p>
 * 
 * <p>
 * This class involves a number of further methods for querying the number of
 * prototypes available, their corresponding IDs, names, as well as payload
 * values.
 * </p>
 * 
 * 
 * @author subere@uncon.org
 * @version 1.9
 * @since 1.2
 */
public class Database {

	// The maximum number of chars to be read from file, regardless
	private static final int MAX_CHARS = Character.MAX_VALUE;
	// The maximum number of lines allowed to be read from the file
	private static final int MAX_LINES = 2048;
	// The maximum length of a line allowed
	private static final int MAX_LINE_LENGTH = 512;

	// The maximum name length for a prototype
	private static final int MAX_PROTO_NAME_LENGTH = Byte.MAX_VALUE;
	// The maximum number of payloads in a prototype
	private static final int MAX_NO_OF_PAYLOADS = Byte.MAX_VALUE;
	// The maximum number of categories of a prototype
	private static final int MAX_NO_OF_CATEGORIES = Byte.MAX_VALUE;

	final private Map<String, Prototype> prototypes;
	
	private final static String ZERO_FUZZER_NAME = "Zero Fuzzers";

	/**
	 * <p>
	 * Constructs a database of prototypes from file and by loading other
	 * fuzzers as well.
	 * </p>
	 * 
	 * 
	 * @see #createFuzzer(String, int)
	 * @author subere@uncon.org
	 * @version 1.5
	 * @since 1.2
	 */
	public Database() {
		
		prototypes = new HashMap<String, Prototype>();
		loadFile();

		// Add the Zero Fuzzers
		final Prototype pt0 = new Prototype('Z', "999-ZER-10K", "10000 Plain Requests");
		final Prototype pt1 = new Prototype('Z', "999-ZER-1KI", "1000 Plain Requests");
		final Prototype pt2 = new Prototype('Z', "999-ZER-100", "100 Plain Requests");
		final Prototype pt3 = new Prototype('Z', "999-ZER-TEN", "10 Plain Requests");
		final Prototype pt4 = new Prototype('Z', "999-ZER-ONE", "1 Plain Request");

		pt0.addCategory(ZERO_FUZZER_NAME);
		pt1.addCategory(ZERO_FUZZER_NAME);
		pt2.addCategory(ZERO_FUZZER_NAME);
		pt3.addCategory(ZERO_FUZZER_NAME);
		pt4.addCategory(ZERO_FUZZER_NAME);

		for (int i = 0; i < 10000; i++) {

			pt0.addPayload("");

			if(i < 1000) {
				pt1.addPayload("");
			}
			if(i < 100) {
				pt2.addPayload("");
			}
			if(i < 10) {
				pt3.addPayload("");
			}
			if(i < 1) {
				pt4.addPayload("");
			}
		}

		prototypes.put("999-ZER-10K", pt0);
		prototypes.put("999-ZER-1KI", pt1);
		prototypes.put("999-ZER-100", pt2);
		prototypes.put("999-ZER-TEN", pt3);
		prototypes.put("999-ZER-ONE", pt4);

	}

	/**
	 * <p>
	 * Checks if the {@link #Database()} contains a Prototype with the given id
	 * 
	 * @param prototypeId
	 *            e.g 001-HTT-MTH or 032-SQL-INJ
	 * 
	 * @return true if a Prototype with that prototypeId exists
	 * 
	 * @author subere@uncon.org
	 * @version 1.8
	 * @since 1.2
	 */
	public boolean containsPrototype(final String prototypeId) {

		return prototypes.containsKey(prototypeId);

	}

	/**
	 * <p>
	 * Method responsible for creating a Fuzzer, based on an existing
	 * prototypeId and length specified.
	 * </p>
	 * 
	 * @param prototypeId
	 *            prototypeId e.g 001-HTT-MTH or 032-SQL-INJ
	 * @param len
	 *            The length of the fuzzer, used for recursive fuzzers
	 * @return org.owasp.jbrofuzz.core#Fuzzer()
	 * @throws NoSuchFuzzerException
	 * 
	 * @see #containsPrototype(String)
	 * 
	 * @author subere@uncon.org
	 * @version 1.8
	 * @since 1.2
	 */
	public Fuzzer createFuzzer(final String prototypeId, final int len)
	throws NoSuchFuzzerException {

		if (!containsPrototype(prototypeId)) {

			throw new NoSuchFuzzerException(StringUtils.abbreviate(prototypeId,
					10)
					+ " : No Such Fuzzer Found in the Database ");

		}

		return new Fuzzer(getPrototype(prototypeId), len);
	}

	/**
	 * <p>
	 * This factory method should be used instead of the #createFuzzer() if the
	 * recursive payloads are more than 16^16 long.
	 * </p>
	 * <p>
	 * As a Fuzzer is an iterator using a long datatype for counting, 
	 * FuzzerBigInteger is an iterator using the BigInteger datatype and thus not
	 * limited to 16^16 number of iterator payloads. 
	 * </p>
	 * <p>
	 * Method responsible for creating a FuzzerBigInteger, based on an existing
	 * prototypeId and length specified.
	 * </p>
	 * 
	 * @param prototypeId
	 *            prototypeId e.g 001-HTT-MTH or 032-SQL-INJ
	 * @param len
	 *            The length of the fuzzer, used for recursive fuzzers
	 * @return org.owasp.jbrofuzz.core#FuzzerBigInteger()
	 * @throws NoSuchFuzzerException
	 * 
	 * @see #containsPrototype(String)
	 * 
	 * @author subere@uncon.org
	 * @version 1.9
	 * @since 1.9
	 */
	public FuzzerBigInteger createFuzzerBigInteger(final String prototypeId, final int len) 
	throws NoSuchFuzzerException {

		if (!containsPrototype(prototypeId)) {

			throw new NoSuchFuzzerException(StringUtils.abbreviate(prototypeId,
					10)
					+ " : No Such Fuzzer Found in the Database ");

		}

		return new FuzzerBigInteger(getPrototype(prototypeId), len);
	}

	/**
	 * <p>Return all the unique categories found across prototypes that are loaded
	 * into the database.</p>
	 * 
	 * <p>Category examples include: "Replacive Fuzzers", "Exploits", etc.</p>
	 * 
	 * @return String[] uniqueCategories
	 * 
	 * @author subere@uncon.org
	 * @version 1.5
	 * @since 1.2
	 */
	public String[] getAllCategories() {

		final HashSet<String> o = new HashSet<String>();

		final String[] ids = getAllPrototypeIDs();
		for (String id : ids) {

			final List<String> catArrayList = prototypes.get(id)
			.getCategories();
			final String[] categoriesArray = new String[catArrayList.size()];
			catArrayList.toArray(categoriesArray);

			for (String cCategory : categoriesArray) {

				o.add(cCategory);

			}

		}

		final String[] uCategoriesArray = new String[o.size()];
		o.toArray(uCategoriesArray);

		return uCategoriesArray;

	}

	/**
	 * <p>Get all the unique Prototype IDs that are loaded in the database.</p>
	 * 
	 * @return String[] e.g. ["001-HTT-MTH", "032-SQL-INJ", ...]
	 * 
	 * @see #getAllFuzzerIDs()
	 * 
	 * @author subere@uncon.org
	 * @version 1.5
	 * @since 1.2
	 */
	public String[] getAllPrototypeIDs() {

		final Set<String> set = prototypes.keySet();
		final String[] output = new String[set.size()];
		return set.toArray(output);

	}

	/**
	 * <p>Get all the unique Fuzzer IDs that are loaded in the database.</p>
	 * 
	 * @return String[] e.g. ["FSE-UPP", "HTT-PMT-EDS", ...]
	 * 
	 * @see #getAllPrototypeIDs()
	 * 
	 * @author subere@uncon.org
	 * @version 1.5
	 * @since 1.5
	 */
	public String[] getAllFuzzerIDs() {

		return getAllPrototypeIDs();

	}

	/**
	 * <p>
	 * Get all the names of the Prototypes that are loaded in the database.
	 * </p>
	 * <p>
	 * The names are not required to be unique, if that is required, use
	 * {@link #getAllPrototypeIDs()}
	 * 
	 * @return String[] e.g. ["Uppercase HTTP Methods", ...
	 * 
	 * @see #getAllPrototypeIDs()
	 * @author subere@uncon.org
	 * @version 1.3
	 * @since 1.2
	 */
	public String[] getAllPrototypeNames() {

		final StringBuffer output = new StringBuffer();

		final Set<String> set = prototypes.keySet();
		final String[] input = new String[set.size()];
		set.toArray(input);

		for (String key : input) {
			output.append(prototypes.get(key).getName());
			output.append('\n');
		}

		return output.toString().split("\n");

	}

	/**
	 * <p>
	 * Returns the Id of a prototype, given its name.
	 * </p>
	 * 
	 * @param name
	 *            e.g. "Uppercase HTTP Methods"
	 * @return String the Id, or "" if the name is not found. e.g. "HTT-PMT-EDS"
	 * 
	 * @see #getName(String)
	 * 
	 * @author subere@uncon.org
	 * @version 1.9
	 * @since 1.2
	 */
	public String getIdFromName(final String name) {

		final String[] ids = getAllPrototypeIDs();
		for (String id : ids) {
			final Prototype cPrototype = prototypes.get(id);
			if (name.equalsIgnoreCase(cPrototype.getName())) {
				return id;
			}
		}
		return "";
	}

	/**
	 * <p>
	 * Returns the name of a prototype, given its Id.
	 * </p>
	 * 
	 * @param uniqId
	 *            e.g. "001-HTT-MTH"
	 * @return String e.g. "Uppercase HTTP Methods"
	 * 
	 * @see #getIdFromName(String)
	 * 
	 * @author subere@uncon.org
	 * @version 1.3
	 * @since 1.2
	 */
	public String getName(final String uniqId) {

		return prototypes.get(uniqId).getName();

	}

	/**
	 * <p>
	 * Returns the array of payloads attached to a given prototype Id.
	 * </p>
	 * 
	 * @param uniqId
	 *            e.g. "001-HTT-MTH"
	 * @return String[] or String[0] if the prototype does not exist in the
	 *         database
	 * 
	 * @see #getSize(String)
	 * 
	 * @author subere@uncon.org
	 * @version 1.3
	 * @since 1.2
	 */
	public String[] getPayloads(final String uniqId) {

		if (containsPrototype(uniqId)) {
			Prototype cPrototype = prototypes.get(uniqId);
			final String[] output = new String[cPrototype.size()];
			return cPrototype.getPayloads().toArray(output);
		} else {
			return new String[0];
		}

	}

	/**
	 * <p>
	 * Return the Prototype, based on the prototypeID given.
	 * </p>
	 * 
	 * @param prototypeId
	 *            e.g. "001-HTT-MTH"
	 * @return Prototype The Prototype for the given prototypeID
	 * 
	 * @author subere@uncon.org
	 * @version 1.3
	 * @since 1.2
	 */
	public Prototype getPrototype(final String prototypeId) {

		return prototypes.get(prototypeId);

	}

	/**
	 * <p>
	 * Given a category, return all prototype names that belong to that
	 * category.
	 * </p>
	 * 
	 * @param category
	 *            the category as a string to check
	 * @return String[] array of prototype names
	 * 
	 * @author subere@uncon.org
	 * @version 1.3
	 * @since 1.2
	 */
	public String[] getPrototypeNamesInCategory(final String category) {

		final HashSet<String> o = new HashSet<String>();
		final String[] ids = getAllPrototypeIDs();

		for (String id : ids) {

			Prototype g = prototypes.get(id);
			if (g.isAMemberOfCategory(category)) {
				o.add(g.getName());
			}
		}

		String[] uCategotiesArray = new String[o.size()];
		o.toArray(uCategotiesArray);

		return uCategotiesArray;
	}

	/**
	 * <p>
	 * Get the number of payloads the prototype with the given Id has.
	 * </p>
	 * 
	 * @param fuzzerId
	 *            e.g. "001-HTT-MTH"
	 * @return int value of size, 0 if Id does not exist.
	 * 
	 * @see #getPayloads(String)
	 * @author subere@uncon.org
	 * @version 1.9
	 * @since 1.2
	 */
	public int getSize(final String fuzzerId) {

		int returnSize = 0;
		
		if (containsPrototype(fuzzerId)) {
			final Prototype g = prototypes.get(fuzzerId);
			returnSize = g.size();
		}
		
		return returnSize;

	}

	/**
	 * <p>
	 * Method called from the constructor to load the prototype definitions of
	 * fuzzers from file.
	 * </p>
	 * 
	 * <p>
	 * This method has a considerable number of checks for loading prototypes
	 * from the internal file, which can be removed, to speed up reading the
	 * file.
	 * </p>
	 * 
	 * @return 0 if all ok, negative values in case of an error
	 * 
	 * @see #Database()
	 * @author subere@uncon.org
	 * @version 1.3
	 * @since 1.3
	 */
	private int loadFile() {

		final StringBuffer fileContents = new StringBuffer();

		// Attempt to read from the jar file
		final URL fileURL = ClassLoader.getSystemClassLoader().getResource(
		"fuzzers.jbrf");

		if (fileURL == null) {
			// -1 File not found
			return -1;
		}

		// Read the characters from the file
		BufferedReader in = null;
		try {
			final URLConnection connection = fileURL.openConnection();
			connection.connect();

			in = new BufferedReader(new InputStreamReader(connection
					.getInputStream()));

			int counter = 0;
			int c;
			while (((c = in.read()) > 0) && (counter < MAX_CHARS)) {
				// Allow the character only if its printable ascii or \n
				if ((CharUtils.isAsciiPrintable((char) c))
						|| (((char) c) == '\n')) {
					fileContents.append((char) c);
				}
				counter++;
			}

			in.close();

		} catch (final IOException e1) {
			// -2 IOException while reading the file
			return -2;
		} finally {
			IOUtils.closeQuietly(in);
		}

		// Break down the file contents into lines
		final String[] fileInput = fileContents.toString().split("\n");

		if (fileInput.length > MAX_LINES) {
			// -3 More lines than you can load
			return -3;
		}

		for (int i = 0; i < fileInput.length; i++) {

			// Ignore comment lines starting with '#'
			if (fileInput[i].startsWith("#")) {
				continue;
			}

			// Ignore lines of length greater than MAX_LINE_LENGTH
			if (fileInput[i].length() > MAX_LINE_LENGTH) {
				continue;
			}

			// Check 1 indicating a likely prototype candidate
			try {
				if (fileInput[i].charAt(1) != ':') {
					continue;
				}
				if (fileInput[i].charAt(13) != ':') {
					continue;
				}

			} catch (IndexOutOfBoundsException e1) {
				continue;
			}

			// [0] -> P || R || X
			// [1] -> "001-HTT-MTH"
			// [2] -> Uppercase HTTP Methods
			// [3] -> 8
			final String[] _fla = fileInput[i].split(":");

			// Check that there are four fields separated by :
			if (_fla.length != 4) {
				continue;
			}

			final char inputTypeChar = _fla[0].charAt(0);

			// Check [0] -> Fuzzer Type 'Z' or 'P', etc..
			if(!Prototype.isValidFuzzerType(inputTypeChar)) {
				continue;
			}

			// The Id: SQL-INJ cannot be empty
			if (_fla[1].isEmpty()) {
				continue;
			}

			// The name: "SQL Injection" cannot be empty
			if (_fla[2].isEmpty()) {
				continue;
			}

			// Check the prototype name length
			if (_fla[2].length() > MAX_PROTO_NAME_LENGTH) {
				continue;
			}

			int noPayloads = 0;
			try {
				noPayloads = Integer.parseInt(_fla[3]);

			} catch (final NumberFormatException e) {
				continue;
			}

			// Check how many payloads this prototype has
			if (noPayloads > MAX_NO_OF_PAYLOADS) {
				continue;
			}

			// Allow only zero fuzzers to have no payloads
			if (noPayloads == 0) {
				continue;
			}

			// Check we have that many payloads left in file
			if (i + noPayloads > fileInput.length) {
				continue;
			}

			try {
				if (!fileInput[i + 1].startsWith(">")) {
					continue;
				}
				if (!fileInput[i + 2].startsWith(">>")) {
					continue;
				}
			} catch (IndexOutOfBoundsException e) {
				continue;
			}

			String line2 = "";
			try {
				line2 = fileInput[i + 1].substring(1);
			} catch (IndexOutOfBoundsException e) {
				continue;
			}

			// [0] -> HTTP Methods
			// [1] -> Replacive Fuzzers
			// [2] -> Uppercase Fuzzers
			final String[] _sla = line2.split("\\|");
			if (_sla.length > MAX_NO_OF_CATEGORIES) {
				continue;
			}

			// Alas! Finally create a prototype
			final Prototype proto = 
				new Prototype(inputTypeChar, _fla[1], _fla[2]);

			// If categories do exist in the second line
			if (_sla.length > 0) {

				for (String categ_ry : _sla) {
					// add the category to the prototype
					categ_ry = StringUtils.stripEnd(categ_ry, " ");
					categ_ry = StringUtils.stripStart(categ_ry, " ");

					if (!categ_ry.isEmpty()) {
						proto.addCategory(categ_ry);
					}

				}
			}
			// If no categories have been identified,
			// add a default category
			else {

				proto.addCategory("JBroFuzz");

			}

			// Add the values of each payload
			for (int j = 1; j <= noPayloads; j++) {
				try {

					proto.addPayload(fileInput[i + 2 + j]);

				} catch (IndexOutOfBoundsException e) {
					continue;
				}
			}

			// Finally add the prototype to the database
			prototypes.put(_fla[1], proto);

		}

		return 0;

	}

	/**
	 * <p>
	 * Return the size of the database.
	 * </p>
	 * 
	 * @return int the size of the database.
	 * 
	 * @author subere@uncon.org
	 * @version 1.3
	 * @since 1.2
	 */
	public int size() {

		return prototypes.size();

	}

	/**
	 * <p>Return the type of fuzzer e.g. Replacive, Recursive, etc.
	 * based on the name provided.</p>
	 * 
	 * @param name The fuzzer name
	 * @return e.g. Replacive
	 */
	public String getTypeFromName(final String name) {

		return prototypes.get(getIdFromName(name)).getType();

	}

	/**
	 * <p>Return the prototype type, based on the id value.</p>
	 * 
	 * @param fuzzerId
	 * @return e.g. "Recursive", "Cross Product"
	 * 
	 * @author subere@uncon.org
	 * @version 1.8
	 * @since 1.8
	 */
	public String getType(final String fuzzerId) {

		return prototypes.get(fuzzerId).getType();

	}

}
