//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v@@BUILD_VERSION@@ 
// 	See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// 	Any modifications to this file will be lost upon recompilation of the source schema. 
// 	Generated on: 2005.11.16 � 06:31:14 CET 
//


package be.ac.ulg.montefiore.run.totem.trafficMatrix.model.jaxb;


/**
 * Java content class for anonymous complex type.
 * 	<p>The following schema fragment specifies the expected 	content contained within this java content object. 	(defined at file:/home/lepropre/projects/run-totem/src/resources/trafficMatrix/TrafficMatrix-v1_2.xsd line 68)
 * <p>
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="src" maxOccurs="unbounded">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="dst" maxOccurs="unbounded">
 *                     &lt;complexType>
 *                       &lt;simpleContent>
 *                         &lt;extension base="&lt;>nonNegativeFloat">
 *                           &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                         &lt;/extension>
 *                       &lt;/simpleContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *                 &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *       &lt;attribute name="ASID" use="required" type="{}ASIdType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 */
public interface IntraTMType {


    /**
     * Gets the value of the asid property.
     * 
     */
    int getASID();

    /**
     * Sets the value of the asid property.
     * 
     */
    void setASID(int value);

    boolean isSetASID();

    void unsetASID();

    /**
     * Gets the value of the Src property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the Src property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSrc().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link be.ac.ulg.montefiore.run.totem.trafficMatrix.model.jaxb.IntraTMType.SrcType}
     * 
     */
    java.util.List getSrc();

    boolean isSetSrc();

    void unsetSrc();


    /**
     * Java content class for anonymous complex type.
     * 	<p>The following schema fragment specifies the expected 	content contained within this java content object. 	(defined at file:/home/lepropre/projects/run-totem/src/resources/trafficMatrix/TrafficMatrix-v1_2.xsd line 71)
     * <p>
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="dst" maxOccurs="unbounded">
     *           &lt;complexType>
     *             &lt;simpleContent>
     *               &lt;extension base="&lt;>nonNegativeFloat">
     *                 &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *               &lt;/extension>
     *             &lt;/simpleContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *       &lt;/sequence>
     *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     */
    public interface SrcType {


        /**
         * Gets the value of the Dst property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the Dst property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getDst().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link be.ac.ulg.montefiore.run.totem.trafficMatrix.model.jaxb.IntraTMType.SrcType.DstType}
         * 
         */
        java.util.List getDst();

        boolean isSetDst();

        void unsetDst();

        /**
         * Gets the value of the id property.
         * 
         * @return
         *     possible object is
         *     {@link java.lang.String}
         */
        java.lang.String getId();

        /**
         * Sets the value of the id property.
         * 
         * @param value
         *     allowed object is
         *     {@link java.lang.String}
         */
        void setId(java.lang.String value);

        boolean isSetId();

        void unsetId();


        /**
         * Java content class for anonymous complex type.
         * 	<p>The following schema fragment specifies the expected 	content contained within this java content object. 	(defined at file:/home/lepropre/projects/run-totem/src/resources/trafficMatrix/TrafficMatrix-v1_2.xsd line 74)
         * <p>
         * <pre>
         * &lt;complexType>
         *   &lt;simpleContent>
         *     &lt;extension base="&lt;>nonNegativeFloat">
         *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
         *     &lt;/extension>
         *   &lt;/simpleContent>
         * &lt;/complexType>
         * </pre>
         * 
         */
        public interface DstType {


            /**
             * Gets the value of the value property.
             * 
             */
            float getValue();

            /**
             * Sets the value of the value property.
             * 
             */
            void setValue(float value);

            boolean isSetValue();

            void unsetValue();

            /**
             * Gets the value of the id property.
             * 
             * @return
             *     possible object is
             *     {@link java.lang.String}
             */
            java.lang.String getId();

            /**
             * Sets the value of the id property.
             * 
             * @param value
             *     allowed object is
             *     {@link java.lang.String}
             */
            void setId(java.lang.String value);

            boolean isSetId();

            void unsetId();

        }

    }

}
