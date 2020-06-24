<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:xsd="http://www.w3.org/2001/XMLSchema"
	xmlns:gwr-bestand="http://www.kadaster.nl/schemas/bag-verstrekkingen/gwr-deelbestand-lvc/v20120701"
	xmlns:selecties-extract="http://www.kadaster.nl/schemas/bag-verstrekkingen/extract-selecties/v20110901"
	xmlns:bagtype="http://www.kadaster.nl/schemas/imbag/imbag-types/v20110901"
	xmlns:gwr-product="http://www.kadaster.nl/schemas/bag-verstrekkingen/gwr-producten-lvc/v20120701"
	xmlns:gwr_LVC="http://www.kadaster.nl/schemas/bag-gwr-model/lvc/v20120701"
	xmlns:gwr_gemeente="http://www.kadaster.nl/schemas/bag-gwr-model/gemeente/v20120701"
	exclude-result-prefixes="gwr-bestand selecties-extract bagtype gwr-product gwr_LVC gwr_gemeente" version="2.0">

	<xsl:output method="xml" encoding="UTF-8" omit-xml-declaration="yes" indent="yes" />
	
	<xsl:template match="gwr-bestand:vraag">
		<extract>
			<datum>
				<xsl:value-of select="selecties-extract:StandTechnischeDatum"/>
			</datum>
		</extract>
	</xsl:template>
</xsl:stylesheet>