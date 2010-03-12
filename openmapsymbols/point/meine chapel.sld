<?xml version="1.0" encoding="ISO-8859-1"?>
<sld:UserStyle xmlns="http://www.opengis.net/sld" xmlns:sld="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc" xmlns:gml="http://www.opengis.net/gml">
  <sld:Name>settlement 2</sld:Name>
  <sld:Title>SK</sld:Title>
  <sld:Abstract>sdsds</sld:Abstract>
  <sld:FeatureTypeStyle>
    <sld:Name>name</sld:Name>
    <sld:Title>title</sld:Title>
    <sld:Abstract>abstract</sld:Abstract>
    <sld:FeatureTypeName>Feature</sld:FeatureTypeName>
    <sld:SemanticTypeIdentifier>generic:geometry</sld:SemanticTypeIdentifier>
    <sld:Rule>
      <sld:MaxScaleDenominator>1.7976931348623157E308</sld:MaxScaleDenominator>
      <sld:PointSymbolizer>
        <sld:Geometry>
          <sld:PropertyName>the_geom</sld:PropertyName>
        </sld:Geometry>
        <sld:Graphic>
          <sld:ExternalGraphic>
            <sld:OnlineResource xmlns:xlink="http://www.w3.org/1999/xlink" xlink:type="simple" xlink:href="http://www.geopublishing.org/openmapsymbols/svg/osm/religion/chapel.svg"/>
            <sld:Format>image/svg+xml</sld:Format>
          </sld:ExternalGraphic>
          <sld:Opacity>
            <ogc:Literal>1.0</ogc:Literal>
          </sld:Opacity>
          <sld:Size>
            <ogc:Literal>24.0</ogc:Literal>
          </sld:Size>
          <sld:Rotation>
            <ogc:Literal>0.0</ogc:Literal>
          </sld:Rotation>
        </sld:Graphic>
      </sld:PointSymbolizer>
      <sld:PointSymbolizer>
        <sld:Geometry>
          <sld:PropertyName>the_geom</sld:PropertyName>
        </sld:Geometry>
        <sld:Graphic>
          <sld:Mark>
            <sld:WellKnownName>square</sld:WellKnownName>
            <sld:Fill>
              <sld:CssParameter name="fill">
                <ogc:Literal>#ff3333</ogc:Literal>
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
                <ogc:Literal>1.0</ogc:Literal>
              </sld:CssParameter>
              <sld:CssParameter name="stroke-width">
                <ogc:Literal>0.5</ogc:Literal>
              </sld:CssParameter>
              <sld:CssParameter name="stroke-dashoffset">
                <ogc:Literal>0.0</ogc:Literal>
              </sld:CssParameter>
            </sld:Stroke>
          </sld:Mark>
          <sld:Opacity>
            <ogc:Literal>1.0</ogc:Literal>
          </sld:Opacity>
          <sld:Size>
            <ogc:Literal>11.0</ogc:Literal>
          </sld:Size>
          <sld:Rotation>
            <ogc:Literal>0.0</ogc:Literal>
          </sld:Rotation>
        </sld:Graphic>
      </sld:PointSymbolizer>
    </sld:Rule>
  </sld:FeatureTypeStyle>
</sld:UserStyle>
