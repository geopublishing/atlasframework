<?xml version="1.0" encoding="UTF-8"?>
<sld:UserStyle xmlns="http://www.opengis.net/sld" xmlns:sld="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc" xmlns:gml="http://www.opengis.net/gml">
  <sld:Name>hashes6</sld:Name>
  <sld:Title>Andrea Henneberger</sld:Title>
  <sld:Abstract>use freely, needs GeoTools 2.6+</sld:Abstract>
  <sld:FeatureTypeStyle>
    <sld:Name>name</sld:Name>
    <sld:FeatureTypeName>Feature</sld:FeatureTypeName>
    <sld:SemanticTypeIdentifier>SemanticType[ANY]</sld:SemanticTypeIdentifier>
    <sld:Rule>
      <sld:MaxScaleDenominator>1.7976931348623157E308</sld:MaxScaleDenominator>
      <sld:PolygonSymbolizer>
        <sld:Geometry>
          <ogc:PropertyName>the_geom</ogc:PropertyName>
        </sld:Geometry>
        <sld:Fill>
          <sld:GraphicFill>
            <sld:Graphic>
              <sld:Mark>
                <sld:WellKnownName>shape://horline</sld:WellKnownName>
                <sld:Fill>
                  <sld:CssParameter name="fill">
                    <ogc:Literal>#808080</ogc:Literal>
                  </sld:CssParameter>
                  <sld:CssParameter name="fill-opacity">
                    <ogc:Literal>1.0</ogc:Literal>
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
                    <ogc:Literal>1</ogc:Literal>
                  </sld:CssParameter>
                  <sld:CssParameter name="stroke-width">
                    <ogc:Literal>1</ogc:Literal>
                  </sld:CssParameter>
                  <sld:CssParameter name="stroke-dashoffset">
                    <ogc:Literal>0</ogc:Literal>
                  </sld:CssParameter>
                </sld:Stroke>
              </sld:Mark>
              <sld:Opacity>
                <ogc:Literal>1.0</ogc:Literal>
              </sld:Opacity>
              <sld:Size>
                <ogc:Literal>8.0</ogc:Literal>
              </sld:Size>
              <sld:Rotation>
                <ogc:Literal>0.0</ogc:Literal>
              </sld:Rotation>
            </sld:Graphic>
          </sld:GraphicFill>
          <sld:CssParameter name="fill">
            <ogc:Literal>#000000</ogc:Literal>
          </sld:CssParameter>
          <sld:CssParameter name="fill-opacity">
            <ogc:Literal>1.0</ogc:Literal>
          </sld:CssParameter>
        </sld:Fill>
      </sld:PolygonSymbolizer>
      <sld:PolygonSymbolizer>
        <sld:Geometry>
          <ogc:PropertyName>the_geom</ogc:PropertyName>
        </sld:Geometry>
        <sld:Fill>
          <sld:CssParameter name="fill">
            <ogc:Literal>#cc66ff</ogc:Literal>
          </sld:CssParameter>
          <sld:CssParameter name="fill-opacity">
            <ogc:Literal>1.0</ogc:Literal>
          </sld:CssParameter>
        </sld:Fill>
      </sld:PolygonSymbolizer>
    </sld:Rule>
  </sld:FeatureTypeStyle>
</sld:UserStyle>
