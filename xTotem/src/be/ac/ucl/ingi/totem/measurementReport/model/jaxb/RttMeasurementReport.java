//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v1.0.4-b18-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2007.03.16 at 03:54:45 CET 
//


package be.ac.ucl.ingi.totem.measurementReport.model.jaxb;


/**
 * Java content class for rttMeasurementReport element declaration.
 * <p>The following schema fragment specifies the expected content contained within this java content object. (defined at file:/biom/biom1/tdekens/workspace/rtt/xml/measurementReport.xsd line 13)
 * <p>
 * <pre>
 * &lt;element name="rttMeasurementReport">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;sequence>
 *           &lt;element name="info" type="{}information"/>
 *           &lt;element name="measures" type="{}subnetMeasures" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;/sequence>
 *       &lt;/restriction>
 *     &lt;/complexContent>
 *   &lt;/complexType>
 * &lt;/element>
 * </pre>
 * 
 */
public interface RttMeasurementReport
    extends javax.xml.bind.Element, be.ac.ucl.ingi.totem.measurementReport.model.jaxb.RttMeasurementReportType
{


}