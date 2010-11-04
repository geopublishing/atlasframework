<?xml version="1.0" encoding="ISO-8859-1"?>
<sld:UserStyle xmlns="http://www.opengis.net/sld" xmlns:sld="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc" xmlns:gml="http://www.opengis.net/gml">
  <sld:Name/>
  <sld:Title>wikisquare.de</sld:Title>
  <sld:Abstract>identifies a missing icon</sld:Abstract>
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
            <sld:OnlineResource xmlns:xlink="http://www.w3.org/1999/xlink" xlink:type="simple" xlink:href="http://www.geopublishing.org/openmapsymbols/svg/osm/misc/no_icon.svg"/>
            <sld:Format>image/svg+xml</sld:Format>
          </sld:ExternalGraphic>
          <sld:Opacity>
            <ogc:Literal>1.0</ogc:Literal>
          </sld:Opacity>
          <sld:Size>
            <ogc:Literal>15.0</ogc:Literal>
          </sld:Size>
          <sld:Rotation>
            <ogc:Literal>0.0</ogc:Literal>
          </sld:Rotation>
        </sld:Graphic>
      </sld:PointSymbolizer>
    </sld:Rule>
  </sld:FeatureTypeStyle>
</sld:UserStyle>
