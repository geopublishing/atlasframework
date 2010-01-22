<?xml version="1.0" encoding="ISO-8859-1" standalone="yes"?>
<sld:StyledLayerDescriptor version="1.0.0" xmlns:sld="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc" xmlns:xlink="http://www.w3.org/1999/xlink">
  <sld:NamedLayer>
    <sld:Name>brunnen_pic</sld:Name>
    <sld:UserStyle>
      <sld:Name>Style1</sld:Name>
      <sld:FeatureTypeStyle>
        <sld:FeatureTypeName>brunnen_pic</sld:FeatureTypeName>
	<sld:Rule>
	  <MinScaleDenominator>1</MinScaleDenominator>
	  <MaxScaleDenominator>5e6</MaxScaleDenominator>

  	  <sld:TextSymbolizer>
		<sld:Label><ogc:PropertyName>ABREVIATIO</ogc:PropertyName></sld:Label>
		<sld:Font>
			<sld:CssParameter name="font-family"><ogc:Literal>Times New Roman</ogc:Literal></sld:CssParameter>
			<sld:CssParameter name="font-size"><ogc:Literal>13.0</ogc:Literal></sld:CssParameter>
			<sld:CssParameter name="font-style"><ogc:Literal>normal</ogc:Literal></sld:CssParameter>
			<sld:CssParameter name="font-weight"><ogc:Literal>normal</ogc:Literal></sld:CssParameter>
		</sld:Font>
		<sld:LabelPlacement>
		   <sld:PointPlacement>
                                <sld:AnchorPoint>
                                    <sld:AnchorPointX>
                                        <ogc:Literal>0.0</ogc:Literal>
                                    </sld:AnchorPointX>
                                    <sld:AnchorPointY>
                                        <ogc:Literal>0.5</ogc:Literal>
                                    </sld:AnchorPointY>
                                </sld:AnchorPoint>
                                <sld:Displacement>
                                    <sld:DisplacementX>
                                        <ogc:Literal>6</ogc:Literal>
                                    </sld:DisplacementX>
                                    <sld:DisplacementY>
                                        <ogc:Literal>0</ogc:Literal>
                                    </sld:DisplacementY>
                                </sld:Displacement>
                                <sld:Rotation>
                                    <ogc:Literal>0.0</ogc:Literal>
                                </sld:Rotation>
                   </sld:PointPlacement>
		</sld:LabelPlacement>
		<sld:Fill>
			<sld:CssParameter name="fill">
			<ogc:Literal>#0062AD</ogc:Literal></sld:CssParameter>
			<sld:CssParameter name="fill-opacity">
			<ogc:Literal>1</ogc:Literal></sld:CssParameter>
		</sld:Fill>
		<Halo>
		<sld:Fill>
			<sld:CssParameter name="fill">
			<ogc:Literal>#FFFFFF</ogc:Literal></sld:CssParameter>
			<sld:CssParameter name="fill-opacity">
			<ogc:Literal>0.8</ogc:Literal></sld:CssParameter>
		</sld:Fill>
		</Halo>
	    </sld:TextSymbolizer>
	</sld:Rule>

        <sld:Rule>

	  <MinScaleDenominator>1</MinScaleDenominator>
	  <MaxScaleDenominator>5e6</MaxScaleDenominator>

          <sld:Name>brunnen_pic</sld:Name>
          <sld:Title>de{Brunnen}en{wells}fr{Puits}</sld:Title>
          <sld:PointSymbolizer>
            <sld:Graphic>
              <sld:Mark>
                <sld:WellKnownName>circle</sld:WellKnownName>
                <sld:Fill>
                  <sld:CssParameter name="fill">#FFFFFF</sld:CssParameter>
                  <sld:CssParameter name="fill-opacity">1</sld:CssParameter>
                </sld:Fill>

                <sld:Stroke>
                  <sld:CssParameter name="stroke">#0062AD</sld:CssParameter>
                  <sld:CssParameter name="stroke-width">1.3</sld:CssParameter>
                  <sld:CssParameter name="stroke-opacity">0.8</sld:CssParameter>
                </sld:Stroke>

              </sld:Mark>
              <sld:Size>8.5</sld:Size>
              <sld:Rotation>0</sld:Rotation>
            </sld:Graphic>
          </sld:PointSymbolizer>

          <sld:PointSymbolizer>
            <sld:Graphic>
              <sld:Mark>
                <sld:WellKnownName>circle</sld:WellKnownName>
                <sld:Fill>
                  <sld:CssParameter name="fill">#0062AD</sld:CssParameter>
                  <sld:CssParameter name="fill-opacity">1.0</sld:CssParameter>
                </sld:Fill>
                <sld:Stroke>
                  <sld:CssParameter name="stroke">#000000</sld:CssParameter>
                  <sld:CssParameter name="stroke-width">1</sld:CssParameter>
                  <sld:CssParameter name="stroke-opacity">1.0</sld:CssParameter>
                </sld:Stroke>
              </sld:Mark>
              <sld:Size>4</sld:Size>
              <sld:Rotation>0</sld:Rotation>
            </sld:Graphic>
          </sld:PointSymbolizer>
        </sld:Rule>
      </sld:FeatureTypeStyle>
    </sld:UserStyle>
  </sld:NamedLayer>
</sld:StyledLayerDescriptor>