package ca.mcgill.sable.soot.attributes;

import java.util.*;

import org.eclipse.swt.graphics.RGB;

/**
 * @author jlhotak
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */
public class SootAttribute {

	private int java_ln;
	private int jimple_ln;
	private int jimpleOffsetStart;
	private int jimpleOffsetEnd;
	private int colorKey;
	private String text;
	private ArrayList textList;
	private ArrayList valueAttrs;
	private String filename;
	private int red;
	private int green;
	private int blue;
	private ArrayList linkList;
	
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("\n");
		sb.append("Java Line: "+java_ln+"\n");
		sb.append("Jimple Line: "+jimple_ln+"\n");
		sb.append("Jimple Offset Start: "+jimpleOffsetStart+"\n");
		sb.append("Jimple Offset End: "+jimpleOffsetEnd+"\n");
		sb.append("Texts: \n");
		sb.append(getAllTextAttrs("\n"));
		
		
		return sb.toString();
	}
	
	private static final String NEWLINE = "\n";
		
	public void addValueAttr(PosColAttribute valAttr){
		if (getValueAttrs() == null){
			setValueAttrs(new ArrayList());
		}
		getValueAttrs().add(valAttr);
	}
	
	public void addLinkAttr(LinkAttribute link){
		if (getLinkList() == null){
			setLinkList(new ArrayList());
		}
		getLinkList().add(link);
		addTextAttr(link.getLabel());
	}
	
	public ArrayList getAllLinkAttrs(){
		return getLinkList();
	}

	public void addTextAttr(String text){
		if (getTextList() == null){
			setTextList(new ArrayList());
		}
		getTextList().add(text);
	}
	
	public StringBuffer getAllTextAttrs(String lineSep){
		StringBuffer sb = new StringBuffer();
		if (getTextList() != null){
			Iterator it = getTextList().iterator();
			while (it.hasNext()){
				String next = (String)it.next();
				if (lineSep.equals("<br>")){
					// implies java tooltip
					next = convertHTMLTags(next);
				}
				sb.append(next);
				sb.append(lineSep);
			}
		}
		return sb;
	}
	
	public String convertHTMLTags(String next){
		if (next == null) return null;
		else {
			//System.out.println("next before replace: "+next);
			next = next.replaceAll("<", "&lt;");
			next = next.replaceAll(">", "&gt;");
			//System.out.println("next after replace: "+next);
			return next;
		}
	}
	
	public RGB getRGBColor(){
		System.out.println("RGB Color: "+getRed()+" "+getGreen()+" "+getBlue());
		return new RGB(getRed(), getGreen(), getBlue());
	}
	
	public boolean attrForJimpleLn(int jimple_ln) {
		if (getJimple_ln() == jimple_ln) return true;
		else return false;
	}
	
	public boolean attrForJavaLn(int java_ln) {
		if (getJava_ln() == java_ln) return true;
		else return false;
	}
	
	public SootAttribute() {
	}
	
	/**
	 * Returns the filename.
	 * @return String
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * Returns the java_ln.
	 * @return int
	 */
	public int getJava_ln() {
		return java_ln;
	}

	/**
	 * Returns the jimple_ln.
	 * @return int
	 */
	public int getJimple_ln() {
		return jimple_ln;
	}

	
	/**
	 * Returns the text.
	 * @return String
	 */
	public String getText() {
		return text;
	}

	/**
	 * Sets the filename.
	 * @param filename The filename to set
	 */
	public void setFilename(String filename) {
		this.filename = filename;
	}

	/**
	 * Sets the java_ln.
	 * @param java_ln The java_ln to set
	 */
	public void setJava_ln(int java_ln) {
		this.java_ln = java_ln;
	}

	/**
	 * Sets the jimple_ln.
	 * @param jimple_ln The jimple_ln to set
	 */
	public void setJimple_ln(int jimple_ln) {
		this.jimple_ln = jimple_ln;
	}


	/**
	 * Sets the text.
	 * @param text The text to set
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * @return
	 */
	public int getColorKey() {
		return colorKey;
	}

	/**
	 * @return
	 */
	public int getJimpleOffsetEnd() {
		return jimpleOffsetEnd;
	}

	/**
	 * @return
	 */
	public int getJimpleOffsetStart() {
		return jimpleOffsetStart;
	}

	/**
	 * @param i
	 */
	public void setColorKey(int i) {
		colorKey = i;
	}

	/**
	 * @param i
	 */
	public void setJimpleOffsetEnd(int i) {
		jimpleOffsetEnd = i;
	}

	/**
	 * @param i
	 */
	public void setJimpleOffsetStart(int i) {
		jimpleOffsetStart = i;
	}

	/**
	 * @return
	 */
	public ArrayList getTextList() {
		return textList;
	}

	/**
	 * @param list
	 */
	public void setTextList(ArrayList list) {
		textList = list;
	}

	/**
	 * @return
	 */
	public ArrayList getValueAttrs() {
		return valueAttrs;
	}

	/**
	 * @param list
	 */
	public void setValueAttrs(ArrayList list) {
		valueAttrs = list;
	}

	/**
	 * @return
	 */
	public int getBlue() {
		return blue;
	}

	/**
	 * @return
	 */
	public int getGreen() {
		return green;
	}



	/**
	 * @return
	 */
	public int getRed() {
		return red;
	}

	/**
	 * @param i
	 */
	public void setBlue(int i) {
		blue = i;
	}

	/**
	 * @param i
	 */
	public void setGreen(int i) {
		green = i;
	}

	

	/**
	 * @param i
	 */
	public void setRed(int i) {
		red = i;
	}

	/**
	 * @return
	 */
	public ArrayList getLinkList() {
		return linkList;
	}

	/**
	 * @param list
	 */
	public void setLinkList(ArrayList list) {
		linkList = list;
	}

}
