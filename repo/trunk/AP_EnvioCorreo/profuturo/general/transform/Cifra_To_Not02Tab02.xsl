<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns:cifra="http://mx.profuturo/iib/nci/CifrasControl/CifrasControlService/v1/types">	
	<xsl:template match="/">
		<xsl:for-each select="cifra:consultarCifrasControlResponse/cifrasControl/cifraControl[tipoValidacion='CONTENIDO']">
			<tr>
				<td>
					<xsl:value-of select="detalle"/>
				</td>
				<td>
					<xsl:value-of select="registrosNoCumplieron"/>
				</td>
				<td>
					<xsl:value-of select="totalErrores"/>
				</td>
			</tr>
		</xsl:for-each>
	</xsl:template>
</xsl:stylesheet>