<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    
    <xsl:output method="xml" encoding="UTF-8" standalone="yes"/>
    <xsl:template match="/">
        <domain xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:noNamespaceSchemaLocation="http://totem.run.montefiore.ulg.ac.be/Schema/Domain-v1_0.xsd">
            <xsl:attribute name="ASID">
                <xsl:value-of select="Network/@id"/>
            </xsl:attribute>
            <info>
                <description>
                    Topology converted from the Unina fromat to the TOTEM format
                    using the XSLT of the University of Liege.
                </description>
                <units>
                    <unit>
                        <xsl:attribute name="type">
                            <xsl:text>bandwidth</xsl:text>
                        </xsl:attribute>
                        <xsl:attribute name="value">
                            <xsl:value-of select="Network/Nodes/Node/Interfaces/Interface/Bandwidth/UoM"/>
                        </xsl:attribute>
                    </unit>
                </units>
            </info>
            <topology>
                <nodes>
                    <xsl:for-each select="///Node">
                        <node>
                            <xsl:attribute name="id">
                                <xsl:value-of select="@id"/>
                            </xsl:attribute>
                            <rid>
                                <xsl:value-of select="@id"/>
                            </rid>
                            <interfaces>
                                <xsl:for-each select="Interfaces/Interface">
                                    <interface>
                                        <xsl:attribute name="id">
                                            <xsl:value-of select="@id"/>
                                        </xsl:attribute>
                                        <ip>
                                            <xsl:attribute name="mask">
                                                <xsl:value-of select="Address"/>
                                                <xsl:text>/32</xsl:text>
                                            </xsl:attribute>
                                            <xsl:value-of select="Address"/>
                                        </ip>
                                    </interface>
                                </xsl:for-each>
                            </interfaces>
                        </node>
                    </xsl:for-each>
                </nodes>
                <links>
                    <xsl:for-each select="///Link">
                        <link>
                            <xsl:variable name="sourceNode">
                                <xsl:value-of select="SourceNode"/>
                            </xsl:variable>
                            
                            <xsl:variable name="sourceIf">
                                <xsl:value-of select="SourceInterface"/>
                            </xsl:variable>
                            
                            <xsl:variable name="destNode">
                                <xsl:value-of select="DestNode"/>
                            </xsl:variable>
                            
                            <xsl:variable name="destIf">
                                <xsl:value-of select="DestInterface"/>
                            </xsl:variable>
                            
                            <xsl:attribute name="id">
                                <xsl:value-of select="$sourceNode"/>
                                <xsl:text>-</xsl:text>
                                <xsl:value-of select="$destNode"/>
                            </xsl:attribute>
                            
                            <from>
                                
                                <xsl:attribute name="node">
                                    <xsl:value-of select="SourceNode"/>
                                </xsl:attribute>
                                
                                <xsl:attribute name="if">
                                    
                                    <xsl:for-each select="../../Nodes/Node">
                                        <xsl:if test="@id=$sourceNode">
                                            
                                            <xsl:for-each select="Interfaces/Interface">
                                                <xsl:if test="Address=$sourceIf">
                                                    <xsl:value-of select="@id"/>
                                                </xsl:if>
                                            </xsl:for-each>
                                            
                                        </xsl:if>
                                    </xsl:for-each>
                                    
                                </xsl:attribute>
                            </from>
                            
                            <to>
                                
                                <xsl:attribute name="node">
                                    <xsl:value-of select="DestNode"/>
                                </xsl:attribute>
                                
                                <xsl:attribute name="if">
                                    
                                    <xsl:for-each select="../../Nodes/Node">
                                        <xsl:if test="@id=$destNode">
                                            
                                            <xsl:for-each select="Interfaces/Interface">
                                                <xsl:if test="Address=$destIf">
                                                    <xsl:value-of select="@id"/>
                                                </xsl:if>
                                            </xsl:for-each>
                                            
                                        </xsl:if>
                                    </xsl:for-each>
                                    
                                </xsl:attribute>
                            </to>
                            <bw>
                                <xsl:for-each select="../../Nodes/Node">
                                    <xsl:if test="@id=$sourceNode">
                                        
                                        <xsl:for-each select="Interfaces/Interface">
                                            <xsl:if test="Address=$sourceIf">
                                                <xsl:value-of select="Bandwidth/Value"/>
                                            </xsl:if>
                                        </xsl:for-each>
                                        
                                    </xsl:if>
                                </xsl:for-each>
                            </bw>
                        </link>
                        
                        <link>
                            <xsl:variable name="sourceNode">
                                <xsl:value-of select="SourceNode"/>
                            </xsl:variable>
                            
                            <xsl:variable name="sourceIf">
                                <xsl:value-of select="SourceInterface"/>
                            </xsl:variable>
                            
                            <xsl:variable name="destNode">
                                <xsl:value-of select="DestNode"/>
                            </xsl:variable>
                            
                            <xsl:variable name="destIf">
                                <xsl:value-of select="DestInterface"/>
                            </xsl:variable>
                            
                            <xsl:attribute name="id">
                                <xsl:value-of select="$destNode"/>
                                <xsl:text>-</xsl:text>
                                <xsl:value-of select="$sourceNode"/>
                            </xsl:attribute>
                            
                            <from>
                                
                                <xsl:attribute name="node">
                                    <xsl:value-of select="DestNode"/>
                                </xsl:attribute>
                                
                                <xsl:attribute name="if">
                                    
                                    <xsl:for-each select="../../Nodes/Node">
                                        <xsl:if test="@id=$destNode">
                                            
                                            <xsl:for-each select="Interfaces/Interface">
                                                <xsl:if test="Address=$destIf">
                                                    <xsl:value-of select="@id"/>
                                                </xsl:if>
                                            </xsl:for-each>
                                            
                                        </xsl:if>
                                    </xsl:for-each>
                                    
                                </xsl:attribute>
                            </from>
                            <to>
                                
                                <xsl:attribute name="node">
                                    <xsl:value-of select="SourceNode"/>
                                </xsl:attribute>
                                
                                <xsl:attribute name="if">
                                    
                                    <xsl:for-each select="../../Nodes/Node">
                                        <xsl:if test="@id=$sourceNode">
                                            
                                            <xsl:for-each select="Interfaces/Interface">
                                                <xsl:if test="Address=$sourceIf">
                                                    <xsl:value-of select="@id"/>
                                                </xsl:if>
                                            </xsl:for-each>
                                            
                                        </xsl:if>
                                    </xsl:for-each>
                                    
                                </xsl:attribute>
                            </to>
                            
                            <bw>
                                <xsl:for-each select="../../Nodes/Node">
                                    <xsl:if test="@id=$sourceNode">
                                        
                                        <xsl:for-each select="Interfaces/Interface">
                                            <xsl:if test="Address=$sourceIf">
                                                <xsl:value-of select="Bandwidth/Value"/>
                                            </xsl:if>
                                        </xsl:for-each>
                                        
                                    </xsl:if>
                                </xsl:for-each>
                            </bw>
                        </link>
                        
                    </xsl:for-each>
                    	
                </links>
            </topology>
        </domain>
    </xsl:template>
    
</xsl:stylesheet>
