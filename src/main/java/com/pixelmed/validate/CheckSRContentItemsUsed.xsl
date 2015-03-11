<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output method="text"/>

<xsl:variable name="newline">
<xsl:text>
</xsl:text>
</xsl:variable>

<xsl:variable name="lowercase" select="'abcdefghijklmnopqrstuvwxyz'" />
<xsl:variable name="uppercase" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'" />

<xsl:template name="describeCode">
	<xsl:param name="cv"/>
	<xsl:param name="csd"/>
	<xsl:param name="cm"/>
	
	<xsl:text>(</xsl:text>
	<xsl:value-of select="$cv"/>
	<xsl:text>,</xsl:text>
	<xsl:value-of select="$csd"/>
	<xsl:text>,"</xsl:text>
	<xsl:value-of select="$cm"/>
	<xsl:text>")</xsl:text>
</xsl:template>

<xsl:template name="buildFullPathInInstanceToCurrentNode">
	<xsl:if test="name(.) != 'DicomStructuredReportContent'">
		<xsl:for-each select="..">
			<xsl:call-template name="buildFullPathInInstanceToCurrentNode"/>
			<xsl:text>/</xsl:text>
		</xsl:for-each>
		<xsl:value-of select="translate(name(.),$lowercase,$uppercase)"/>
		<xsl:choose>
		<xsl:when test="string-length(concept/@cv) != 0">
			<xsl:text> </xsl:text>
			<xsl:call-template name="describeCode">
				<xsl:with-param name="cv" select="concept/@cv"/>
				<xsl:with-param name="csd" select="concept/@csd"/>
				<xsl:with-param name="cm" select="concept/@cm"/>
			</xsl:call-template>
		</xsl:when>
		</xsl:choose>
	</xsl:if>
</xsl:template>

<xsl:key name="founditemindex" match="item" use="@location"/>

<xsl:variable name="foundItemsDocument" select="document('FoundItems.xml')/founditems"/>

<xsl:template match="node()[@ID]">
	<xsl:variable name="location" select="substring-after(@ID,'ci_')"/>
	<!-- <xsl:text>2nd pass: template = </xsl:text><xsl:value-of select="$location"/><xsl:value-of select="$newline"/> -->
	<xsl:variable name="wasUsed">
	<xsl:apply-templates select="$foundItemsDocument">
	<xsl:with-param name="keyToFind" select="$location"/>
	</xsl:apply-templates>
	</xsl:variable>
	<!-- <xsl:text>2nd pass: lookup returns = </xsl:text><xsl:value-of select="$wasUsed"/><xsl:value-of select="$newline"/> -->
	<xsl:if test="$wasUsed = 0">
		<xsl:variable name="locationdescription"><xsl:value-of select="$location"/><xsl:text>: </xsl:text><xsl:call-template name="buildFullPathInInstanceToCurrentNode"/></xsl:variable>
		<xsl:text>Warning: </xsl:text><xsl:value-of select="$locationdescription"/><xsl:text>: Content Item not in template</xsl:text><xsl:value-of select="$newline"/>
	</xsl:if>
	<xsl:apply-templates/>
</xsl:template>

<xsl:template match="founditems">
	<xsl:param name="keyToFind"/>
	<xsl:value-of select="count(key('founditemindex', $keyToFind)/@location)"/>
</xsl:template>

<xsl:template match="text()"/>

</xsl:stylesheet>
