/**
 * Constructor.java 0.6
 *
 * Java Bro Fuzzer. A stateless network protocol fuzzer for penetration tests.
 * It allows for the identification of certain classes of security bugs, by
 * means of creating malformed data and having the network protocol in question
 * consume the data.
 *
 * Copyright (C) 2007 subere (at) uncon org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.owasp.jbrofuzz.fuzz;

import java.util.ArrayList;

import org.owasp.jbrofuzz.JBroFuzz;
import org.owasp.jbrofuzz.fuzz.tcp.Generator;
import org.owasp.jbrofuzz.io.FileHandler;
import org.owasp.jbrofuzz.version.JBRFormat;

/**
 * <p>
 * The Constructor constructs a list of Definitions, iterating though each
 * Generator and accumulating values inputted from the corresponding file. This
 * file is located at runtime and should be in the same directory location from
 * which the jar file is launched.
 * </p>
 * 
 * @author subere (at) uncon (dot) org
 * @version 0.6
 */
public class TConstructor {
	final private JBroFuzz mJBroFuzz;
	private ArrayList<Generator> generators;

	/**
	 * <p>
	 * The main constructor responsible for piecing together a definitions class
	 * holding all the generators.
	 * </p>
	 * 
	 * @param mJBroFuzz
	 *          JBroFuzz
	 */
	public TConstructor(final JBroFuzz mJBroFuzz) {
		this.mJBroFuzz = mJBroFuzz;
		generators = new ArrayList<Generator>();

		final StringBuffer fileContents = FileHandler
				.readGenerators(JBRFormat.FILE_GEN);
		final String[] fileInput = fileContents.toString().split("\n");
		final int len = fileInput.length;

		for (int i = 0; i < len; i++) {
			boolean firstLineOk = false;

			final String line = fileInput[i];
			if (line.startsWith("#")) {
				// Comment line hit, do nothing
			} else {
				if (line.length() > 5) {
					// "P|ABC"
					if ((line.charAt(1) == ':') && (line.charAt(5) == ':')) {
						final String[] firstLineArray = line.split(":");
						// Check that there are four fields of | in the first line
						if (firstLineArray.length == 4) {
							// Check that the comment is less than 24 characters
							if ((firstLineArray[2].length() < 25)
									&& (firstLineArray[2].length() > 0)) {
								// Check that the first character is either a P or an R
								if (("P".equals(firstLineArray[0]))
										|| ("R".equals(firstLineArray[0]))) {
									// Check that the length is a positive number < 32
									int generatorLength;
									try {
										generatorLength = Integer.parseInt(firstLineArray[3]);
										if ((generatorLength > 0) && (generatorLength < 33)) {
											firstLineOk = true;
										}
									} catch (final NumberFormatException e) {
										firstLineOk = false;
										generatorLength = 0;
									}
								}
							}
						}
					}
				} // First line check
				if (firstLineOk) {
					final String[] firstArray = line.split(":");
					final int generatorLength = Integer.parseInt(firstArray[3]);
					// Check that there remaining element in the generator Vector
					if (i < len - generatorLength - 1) {
						// Check that the second line starts with a #
						String line2 = fileInput[i + 1];
						if (line2.startsWith(">")) {
							line2 = line2.substring(1);
							// Check to see that the Generator name is unique
							if (!isGeneratorNameUsed(firstArray[1])) {

								// Finally create the generator if all the checks pass
								final Generator myGen = new Generator(firstArray[0].charAt(0),
										firstArray[1], firstArray[2], generatorLength, line2);

								// Add the values for each element
								for (int j = 1; j <= generatorLength; j++) {

									final StringBuffer myBuffer = new StringBuffer();
									myBuffer.append(fileInput[i + 1 + j]);
									myGen.addAlphabetValue(myBuffer);

								}
								// Finally add the generator to the Vector of generators
								generators.add(myGen);
							}
						}
					}
				}
			}
		}
/*
		mJBroFuzz.getWindow().getDefinitionsPanel().setDefinitionsText(
				this.getAllGeneratorNames() + "\n\n");

		mJBroFuzz.getWindow().getDefinitionsPanel().setDefinitionsText(
				this.getAllGenerators());
*/
	}

	/**
	 * <p>
	 * Return the list of generator names that are currently within the
	 * Constructor. The output format is: {ABC, DEF, EFE}
	 * </p>
	 * 
	 * @return String
	 */
	public String getAllGeneratorNames() {
		final StringBuffer output = new StringBuffer();
		output.append("{");
		for (int i = 0; i < generators.size(); i++) {
			output.append(((generators.get(i))).getName());
			if (i < generators.size() - 1) {
				output.append(", ");
			}
		}
		output.append("}");
		return output.toString();
	}

	/**
	 * <p>
	 * Return the list of generators and their comments that are within the
	 * constructor. The output format is: BIN (Binary), OCT (Octal) ...
	 * </p>
	 * 
	 * @return String
	 */
	public String getAllGeneratorNamesAndComments() {
		final StringBuffer output = new StringBuffer();
		for (int i = 0; i < generators.size(); i++) {
			output.append(((generators.get(i))).getName());
			output.append(" (");
			output.append(((generators.get(i))).getComment());
			output.append(")");
			if (i < generators.size() - 1) {
				output.append(", ");
			}
		}
		return output.toString();
	}

	/**
	 * <p>
	 * Return the complete lists of generators, with their alphabet that are
	 * currently within the Constructor.
	 * </p>
	 * <p>
	 * If an alphabet element has more than 50 characters, truncate, showing the
	 * total length.
	 * </p>
	 * 
	 * @return String
	 */
	public String getAllGenerators() {
		final StringBuffer output = new StringBuffer();
		for (int i = 0; i < generators.size(); i++) {
			output.append(((generators.get(i))).getName());
			output.append(":");
			output.append(((generators.get(i))).getComment());
			output.append("\n");
			final int cGenSize = ((generators.get(i))).getSize();
			for (int j = 0; j < cGenSize; j++) {
				final StringBuffer cur = ((generators.get(i)))
						.getElement(j);
				if (cur.length() <= 65) {
					output.append(cur);
				} else {
					final int cLen = ((generators.get(i))).getElement(j)
							.length();
					final String sCur = cur.substring(0, 65);
					output.append(sCur);
					output.append("    (... Total length: " + cLen + ")");
				}
				output.append("\n");
			}
			output.append("\n");
		}
		return output.toString();
	}

	/**
	 * <p>
	 * Return the specified element wihtin a Generator in the form of a
	 * StringBuffer. If the generator, or element at index is not found a newly
	 * constructed StringBuffer is returned.
	 * </p>
	 * 
	 * @param name
	 *          String
	 * @param index
	 *          int
	 * @return StringBuffer
	 */
	public StringBuffer getGeneratorElement(final String name, final int index) {
		StringBuffer output = new StringBuffer();
		for (int i = 0; i < generators.size(); i++) {
			final String currentName = ((generators.get(i)))
					.getName();
			if (currentName.equals(name)) {
				final Generator currentGenerator = (generators.get(i));
				output = currentGenerator.getElement(index);
				i = generators.size();
			}
		}
		return output;
	}

	/**
	 * <p>
	 * Return the length in terms of number of elements of the generator specified
	 * by the given name. If the given name does not correspond to a generator
	 * return 0.
	 * </p>
	 * 
	 * @param name
	 *          String
	 * @return int
	 */
	public int getGeneratorLength(final String name) {
		int output = 0;
		for (int i = 0; i < generators.size(); i++) {
			final String currentName = ((generators.get(i)))
					.getName();
			if (currentName.equals(name)) {
				final Generator currentGenerator = (generators.get(i));
				output = currentGenerator.getSize();
				i = generators.size();
			}
		}
		return output;
	}

	/**
	 * <p>
	 * Return the type of the generator specified by the given name. If the given
	 * name does not correspond to a generator, return the default unknown type.
	 * </p>
	 * 
	 * @param name
	 *          String
	 * @return char
	 */
	public char getGeneratorType(final String name) {
		char output = Generator.UNKNOWN;
		for (int i = 0; i < generators.size(); i++) {
			final String currentName = ((generators.get(i)))
					.getName();
			if (currentName.equals(name)) {
				final Generator currentGenerator = (generators.get(i));
				output = currentGenerator.getType();
				i = generators.size();
			}
		}
		return output;
	}

	/**
	 * <p>
	 * Method for accessing the main JBroFuzz object.
	 * </p>
	 * 
	 * @return JBroFuzz
	 */
	public JBroFuzz getJBroFuzz() {
		return mJBroFuzz;
	}

	/**
	 * <p>
	 * Check if the given String is already a valid Generator name; if so return
	 * true.
	 * </p>
	 * 
	 * @param name
	 *          String
	 * @return boolean
	 */
	public boolean isGeneratorNameUsed(final String name) {
		boolean output = false;
		for (int i = 0; i < generators.size(); i++) {
			final String currentName = ((generators.get(i)))
					.getName();
			if (currentName.equals(name)) {
				output = true;
				i = generators.size();
			}
		}
		return output;
	}
}