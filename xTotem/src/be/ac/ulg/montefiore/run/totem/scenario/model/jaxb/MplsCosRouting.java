//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v1.0.4-b18-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2008.03.28 at 12:33:07 CET 
//


package be.ac.ulg.montefiore.run.totem.scenario.model.jaxb;


/**
 * Java content class for mplsCosRouting element declaration.
 * <p>The following schema fragment specifies the expected content contained within this java content object. (defined at file:/home/hakan/svn-totem/totem/trunk/run-totem/src/resources/scenario/Scenario-v1_3.xsd line 522)
 * <p>
 * <pre>
 * &lt;element name="mplsCosRouting">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;extension base="{http://jaxb.model.scenario.totem.run.montefiore.ulg.ac.be}ASEventType">
 *         &lt;sequence>
 *           &lt;element name="cos" maxOccurs="unbounded">
 *             &lt;complexType>
 *               &lt;simpleContent>
 *                 &lt;extension base="&lt;http://jaxb.model.scenario.totem.run.montefiore.ulg.ac.be>TMIdType">
 *                   &lt;attribute name="name" use="required" type="{http://jaxb.model.scenario.totem.run.montefiore.ulg.ac.be}cosType" />
 *                 &lt;/extension>
 *               &lt;/simpleContent>
 *             &lt;/complexType>
 *           &lt;/element>
 *         &lt;/sequence>
 *         &lt;attribute name="llcId" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;/extension>
 *     &lt;/complexContent>
 *   &lt;/complexType>
 * &lt;/element>
 * </pre>
 * 
 */
public interface MplsCosRouting
    extends javax.xml.bind.Element, be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.MplsCosRoutingType
{


}
