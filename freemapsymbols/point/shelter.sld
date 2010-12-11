<?xml version="1.0" encoding="UTF-8"?>
<sld:UserStyle xmlns="http://www.opengis.net/sld" xmlns:sld="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc" xmlns:gml="http://www.opengis.net/gml">
  <sld:Name>shelter</sld:Name>
  <sld:Title>OSM</sld:Title>
  <sld:Abstract>shelter.svg from OSM.org</sld:Abstract>
  <sld:FeatureTypeStyle>
    <sld:Name>name</sld:Name>
    <sld:Rule>
      <sld:PointSymbolizer>
        <sld:Geometry>
          <ogc:PropertyName>the_geom</ogc:PropertyName>
        </sld:Geometry>
        <sld:Graphic>
          <sld:ExternalGraphic>
            <sld:OnlineResource xmlns:xlink="http://www.w3.org/1999/xlink" xlink:type="simple" xlink:href="http://freemapsymbols.org/svg/osm/accommodation/shelter.svg"/>
            <sld:Format>image/svg+xml</sld:Format>
          </sld:ExternalGraphic>
          <sld:Size>
            <ogc:Literal>35.0</ogc:Literal>
          </sld:Size>
        </sld:Graphic>
      </sld:PointSymbolizer>
    </sld:Rule>
  </sld:FeatureTypeStyle>
</sld:UserStyle>
