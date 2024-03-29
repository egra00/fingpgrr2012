//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v1.0.4-b18-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2008.02.29 at 02:59:16 CET 
//


package be.ac.ulg.montefiore.run.totem.domain.model.jaxb;


/**
 * Java content class for bgpNeighbor complex type.
 * <p>The following schema fragment specifies the expected content contained within this java content object. (defined at file:/home/monfort/Projects/run-totem/src/resources/domain/Domain-v1_3.xsd line 454)
 * <p>
 * <pre>
 * &lt;complexType name="bgpNeighbor">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="filters" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="in-filter" type="{}bgpFilter" minOccurs="0"/>
 *                   &lt;element name="out-filter" type="{}bgpFilter" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *       &lt;attribute name="as" use="required" type="{}ASIdType" />
 *       &lt;attribute name="ip" use="required" type="{}IPAddress" />
 *       &lt;attribute name="next-hop-self" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="reflector-client" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 */
public interface BgpNeighbor {


    /**
     * Gets the value of the nextHopSelf property.
     * 
     */
    boolean isNextHopSelf();

    /**
     * Sets the value of the nextHopSelf property.
     * 
     */
    void setNextHopSelf(boolean value);

    boolean isSetNextHopSelf();

    void unsetNextHopSelf();

    /**
     * Gets the value of the reflectorClient property.
     * 
     */
    boolean isReflectorClient();

    /**
     * Sets the value of the reflectorClient property.
     * 
     */
    void setReflectorClient(boolean value);

    boolean isSetReflectorClient();

    void unsetReflectorClient();

    /**
     * Gets the value of the filters property.
     * 
     * @return
     *     possible object is
     *     {@link be.ac.ulg.montefiore.run.totem.domain.model.jaxb.BgpNeighbor.FiltersType}
     */
    be.ac.ulg.montefiore.run.totem.domain.model.jaxb.BgpNeighbor.FiltersType getFilters();

    /**
     * Sets the value of the filters property.
     * 
     * @param value
     *     allowed object is
     *     {@link be.ac.ulg.montefiore.run.totem.domain.model.jaxb.BgpNeighbor.FiltersType}
     */
    void setFilters(be.ac.ulg.montefiore.run.totem.domain.model.jaxb.BgpNeighbor.FiltersType value);

    boolean isSetFilters();

    void unsetFilters();

    /**
     * Gets the value of the as property.
     * 
     */
    int getAs();

    /**
     * Sets the value of the as property.
     * 
     */
    void setAs(int value);

    boolean isSetAs();

    void unsetAs();

    /**
     * Gets the value of the ip property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String}
     */
    java.lang.String getIp();

    /**
     * Sets the value of the ip property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String}
     */
    void setIp(java.lang.String value);

    boolean isSetIp();

    void unsetIp();


    /**
     * Java content class for anonymous complex type.
     * <p>The following schema fragment specifies the expected content contained within this java content object. (defined at file:/home/monfort/Projects/run-totem/src/resources/domain/Domain-v1_3.xsd line 457)
     * <p>
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="in-filter" type="{}bgpFilter" minOccurs="0"/>
     *         &lt;element name="out-filter" type="{}bgpFilter" minOccurs="0"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     */
    public interface FiltersType {


        /**
         * Gets the value of the inFilter property.
         * 
         * @return
         *     possible object is
         *     {@link be.ac.ulg.montefiore.run.totem.domain.model.jaxb.BgpFilter}
         */
        be.ac.ulg.montefiore.run.totem.domain.model.jaxb.BgpFilter getInFilter();

        /**
         * Sets the value of the inFilter property.
         * 
         * @param value
         *     allowed object is
         *     {@link be.ac.ulg.montefiore.run.totem.domain.model.jaxb.BgpFilter}
         */
        void setInFilter(be.ac.ulg.montefiore.run.totem.domain.model.jaxb.BgpFilter value);

        boolean isSetInFilter();

        void unsetInFilter();

        /**
         * Gets the value of the outFilter property.
         * 
         * @return
         *     possible object is
         *     {@link be.ac.ulg.montefiore.run.totem.domain.model.jaxb.BgpFilter}
         */
        be.ac.ulg.montefiore.run.totem.domain.model.jaxb.BgpFilter getOutFilter();

        /**
         * Sets the value of the outFilter property.
         * 
         * @param value
         *     allowed object is
         *     {@link be.ac.ulg.montefiore.run.totem.domain.model.jaxb.BgpFilter}
         */
        void setOutFilter(be.ac.ulg.montefiore.run.totem.domain.model.jaxb.BgpFilter value);

        boolean isSetOutFilter();

        void unsetOutFilter();

    }

}
