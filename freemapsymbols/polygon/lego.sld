<?xml version="1.0" encoding="UTF-8"?>
<sld:UserStyle xmlns="http://www.opengis.net/sld" xmlns:sld="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc" xmlns:gml="http://www.opengis.net/gml">
  <sld:Name>lego</sld:Name>
  <sld:Title>Arthur Rachowka</sld:Title>
  <sld:Abstract/>
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
          <sld:GraphicFill>
            <sld:Graphic>
              <sld:Mark>
                <sld:WellKnownName>square</sld:WellKnownName>
                <sld:Fill>
                  <sld:CssParameter name="fill">
                    <ogc:Literal>#3366ff</ogc:Literal>
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
                <ogc:Literal>6</ogc:Literal>
              </sld:Size>
              <sld:Rotation>
                <ogc:Literal>10.0</ogc:Literal>
              </sld:Rotation>
            </sld:Graphic>
          </sld:GraphicFill>
          <sld:CssParameter name="fill">
            <ogc:Literal>#3366ff</ogc:Literal>
          </sld:CssParameter>
          <sld:CssParameter name="fill-opacity">
            <ogc:Literal>0.7</ogc:Literal>
          </sld:CssParameter>
        </sld:Fill>
      </sld:PolygonSymbolizer>
      <sld:PolygonSymbolizer>
        <sld:Geometry>
          <sld:PropertyName>the_geom</sld:PropertyName>
        </sld:Geometry>
        <sld:Fill>
          <sld:CssParameter name="fill">
            <ogc:Literal>#3366ff</ogc:Literal>
          </sld:CssParameter>
          <sld:CssParameter name="fill-opacity">
            <ogc:Literal>0.1</ogc:Literal>
          </sld:CssParameter>
        </sld:Fill>
      </sld:PolygonSymbolizer>
    </sld:Rule>
  </sld:FeatureTypeStyle>
</sld:UserStyle>
