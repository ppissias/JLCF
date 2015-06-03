//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.08.26 at 12:34:52 PM CEST 
//


package org.jlcf.core.types.xml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Component complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Component">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="interface" type="{http://jlcf.sourceforge.net/JLCFApplication}Interface" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="receptacle" type="{http://jlcf.sourceforge.net/JLCFApplication}Receptacle" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="property" type="{http://jlcf.sourceforge.net/JLCFApplication}Property" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="implementationClass" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Component", propOrder = {
    "_interface",
    "receptacle",
    "property"
})
public class Component {

    @XmlElement(name = "interface")
    protected List<Interface> _interface;
    protected List<Receptacle> receptacle;
    protected List<Property> property;
    @XmlAttribute
    protected String name;
    @XmlAttribute
    protected String implementationClass;

    /**
     * Gets the value of the interface property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the interface property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInterface().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Interface }
     * 
     * 
     */
    public List<Interface> getInterface() {
        if (_interface == null) {
            _interface = new ArrayList<Interface>();
        }
        return this._interface;
    }

    /**
     * Gets the value of the receptacle property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the receptacle property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getReceptacle().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Receptacle }
     * 
     * 
     */
    public List<Receptacle> getReceptacle() {
        if (receptacle == null) {
            receptacle = new ArrayList<Receptacle>();
        }
        return this.receptacle;
    }

    /**
     * Gets the value of the property property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the property property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getProperty().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Property }
     * 
     * 
     */
    public List<Property> getProperty() {
        if (property == null) {
            property = new ArrayList<Property>();
        }
        return this.property;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the implementationClass property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getImplementationClass() {
        return implementationClass;
    }

    /**
     * Sets the value of the implementationClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setImplementationClass(String value) {
        this.implementationClass = value;
    }

}
