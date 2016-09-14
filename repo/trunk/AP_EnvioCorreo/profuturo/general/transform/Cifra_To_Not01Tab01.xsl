<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns:cifra="http://mx.profuturo/iib/nci/CifrasControl/CifrasControlService/v1/types">
	<xsl:output method="xml"/>	
	<xsl:template match="/">
		<table>
		<xsl:for-each select="cifra:consultarCifrasControlResponse/cifrasControl/cifraControl[tipoValidacion='ESTRUCTURA' and registrosNoCumplieron > 0]">
			<tr>
				<td>
					<xsl:value-of select="position()"/>
				</td>
				<td>
					<xsl:value-of select="error"/>
				</td>
			</tr>
		</xsl:for-each>
		</table>
	</xsl:template>
</xsl:stylesheet>