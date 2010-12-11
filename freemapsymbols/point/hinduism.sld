<?xml version="1.0" encoding="UTF-8"?>
<sld:UserStyle xmlns="http://www.opengis.net/sld" xmlns:sld="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc" xmlns:gml="http://www.opengis.net/gml">
  <sld:Name/>
  <sld:Title>wikisquare.de</sld:Title>
  <sld:Abstract>SVG with SLD circle in bg</sld:Abstract>
  <sld:FeatureTypeStyle>
    <sld:Name>name</sld:Name>
    <sld:Rule>
      <sld:PointSymbolizer>
        <sld:Geometry>
          <ogc:PropertyName>the_geom</ogc:PropertyName>
        </sld:Geometry>
        <sld:Graphic>
          <sld:ExternalGraphic>
            <sld:OnlineResource xmlns:xlink="http://www.w3.org/1999/xlink" xlink:type="simple" xlink:href="http://freemapsymbols.org/svg/osm/religion/church/hinduism.svg"/>
            <sld:Format>image/svg+xml</sld:Format>
          </sld:ExternalGraphic>
          <sld:Size>
            <ogc:Literal>21.0</ogc:Literal>
          </sld:Size>
        </sld:Graphic>
      </sld:PointSymbolizer>
      <sld:PointSymbolizer>
        <sld:Graphic>
          <sld:Mark>
            <sld:WellKnownName>circle</sld:WellKnownName>
            <sld:Fill>
              <sld:CssParameter name="fill">#FF6600</sld:CssParameter>
              <sld:CssParameter name="fill-opacity">0.2</sld:CssParameter>
            </sld:Fill>
          </sld:Mark>
          <sld:Size>
            <ogc:Literal>22.0</ogc:Literal>
          </sld:Size>
          <sld:Rotation>
            <ogc:Literal>5.0</ogc:Literal>
          </sld:Rotation>
        </sld:Graphic>
      </sld:PointSymbolizer>
    </sld:Rule>
  </sld:FeatureTypeStyle>
</sld:UserStyle>
