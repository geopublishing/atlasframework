<?xml version="1.0" encoding="UTF-8"?>
<sld:UserStyle xmlns="http://www.opengis.net/sld" xmlns:sld="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc" xmlns:gml="http://www.opengis.net/gml">
  <sld:Name>trees</sld:Name>
  <sld:Title>Stefan A. Krüger</sld:Title>
  <sld:Abstract>light trees</sld:Abstract>
  <sld:FeatureTypeStyle>
    <sld:Name>name</sld:Name>
    <sld:Title>title</sld:Title>
    <sld:Abstract>abstract</sld:Abstract>
    <sld:FeatureTypeName>Feature</sld:FeatureTypeName>
    <sld:SemanticTypeIdentifier>generic:geometry</sld:SemanticTypeIdentifier>
    <sld:Rule>
      <sld:MaxScaleDenominator>1.7976931348623157E308</sld:MaxScaleDenominator>
      <sld:PolygonSymbolizer>
        <sld:Geometry>
          <sld:PropertyName>the_geom</sld:PropertyName>
        </sld:Geometry>
        <sld:Fill>
          <sld:CssParameter name="fill">
            <ogc:Literal>#66cc00</ogc:Literal>
          </sld:CssParameter>
          <sld:CssParameter name="fill-opacity">
            <ogc:Literal>0.1</ogc:Literal>
          </sld:CssParameter>
        </sld:Fill>
        <sld:Stroke>
          <sld:CssParameter name="stroke">
            <ogc:Literal>#000000</ogc:Literal>
          </sld:CssParameter>
          <sld:CssParameter name="stroke-linecap">
            <ogc:Literal>butt</ogc:Literal>
          </sld:CssParameter>
          <sld:CssParameter name="stroke-linejoin">
            <ogc:Literal>miter</ogc:Literal>
          </sld:CssParameter>
          <sld:CssParameter name="stroke-opacity">
            <ogc:Literal>1.0</ogc:Literal>
          </sld:CssParameter>
          <sld:CssParameter name="stroke-width">
            <ogc:Literal>1.0</ogc:Literal>
          </sld:CssParameter>
          <sld:CssParameter name="stroke-dashoffset">
            <ogc:Literal>0.0</ogc:Literal>
          </sld:CssParameter>
        </sld:Stroke>
      </sld:PolygonSymbolizer>
      <sld:PolygonSymbolizer>
        <sld:Geometry>
          <sld:PropertyName>the_geom</sld:PropertyName>
        </sld:Geometry>
        <sld:Fill>
          <sld:GraphicFill>
            <sld:Graphic>
              <sld:Mark>
                <sld:WellKnownName>triangle</sld:WellKnownName>
                <sld:Fill>
                  <sld:CssParameter name="fill">
                    <ogc:Literal>#00ff00</ogc:Literal>
                  </sld:CssParameter>
                  <sld:CssParameter name="fill-opacity">
                    <ogc:Literal>1.0</ogc:Literal>
                  </sld:CssParameter>
                </sld:Fill>
              </sld:Mark>
              <sld:Opacity>
                <ogc:Literal>1.</ogc:Literal>
              </sld:Opacity>
              <sld:Size>
                <ogc:Literal>10.0</ogc:Literal>
              </sld:Size>
              <sld:Rotation>
                <ogc:Literal>0.0</ogc:Literal>
              </sld:Rotation>
            </sld:Graphic>
          </sld:GraphicFill>
          <sld:CssParameter name="fill">
            <ogc:Literal>#00ff00</ogc:Literal>
          </sld:CssParameter>
          <sld:CssParameter name="fill-opacity">
            <ogc:Literal>1.0</ogc:Literal>
          </sld:CssParameter>
        </sld:Fill>
      </sld:PolygonSymbolizer>
    </sld:Rule>
  </sld:FeatureTypeStyle>
</sld:UserStyle>
