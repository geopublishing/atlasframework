<?xml version="1.0" encoding="UTF-8"?>
<sld:UserStyle xmlns="http://www.opengis.net/sld" xmlns:sld="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc" xmlns:gml="http://www.opengis.net/gml">
  <sld:Name>AtlasStyler v1.4, b704 (Thu Feb 11 19:53:00 CET 2010)</sld:Name>
  <sld:Title>wikisquare.de</sld:Title>
  <sld:FeatureTypeStyle>
    <sld:Name>QUANTITIES_COLORIZED_POLYGON:VALUE#demstat:NORM#null:METHOD#QUANTILES:PALETTE#RdBu</sld:Name>
    <sld:FeatureTypeName>join10</sld:FeatureTypeName>
    <sld:SemanticTypeIdentifier>SemanticType[ANY]</sld:SemanticTypeIdentifier>
    <sld:Rule>
      <sld:Name>AS: 1/5 GraduatedColorPolygonRuleList</sld:Name>
      <sld:Title>en{[0.000 - 2.850[}</sld:Title>
      <ogc:Filter>
        <ogc:And>
          <ogc:Not>
            <ogc:Or>
              <ogc:PropertyIsNull>
                <ogc:PropertyName>demstat</ogc:PropertyName>
              </ogc:PropertyIsNull>
              <ogc:PropertyIsEqualTo>
                <ogc:PropertyName>demstat</ogc:PropertyName>
                <ogc:Literal>0.0</ogc:Literal>
              </ogc:PropertyIsEqualTo>
            </ogc:Or>
          </ogc:Not>
          <ogc:PropertyIsBetween>
            <ogc:PropertyName>demstat</ogc:PropertyName>
            <ogc:LowerBoundary>
              <ogc:Literal>0.0</ogc:Literal>
            </ogc:LowerBoundary>
            <ogc:UpperBoundary>
              <ogc:Literal>2.849999905</ogc:Literal>
            </ogc:UpperBoundary>
          </ogc:PropertyIsBetween>
        </ogc:And>
      </ogc:Filter>
      <sld:MinScaleDenominator>4.9E-324</sld:MinScaleDenominator>
      <sld:MaxScaleDenominator>1.7976931348623157E308</sld:MaxScaleDenominator>
      <sld:PolygonSymbolizer>
        <sld:Geometry>
          <ogc:PropertyName>the_geom</ogc:PropertyName>
        </sld:Geometry>
        <sld:Fill>
          <sld:CssParameter name="fill">
            <ogc:Literal>#0571B0</ogc:Literal>
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
            <ogc:Literal>1.0</ogc:Literal>
          </sld:CssParameter>
          <sld:CssParameter name="stroke-dashoffset">
            <ogc:Literal>0.0</ogc:Literal>
          </sld:CssParameter>
        </sld:Stroke>
      </sld:PolygonSymbolizer>
    </sld:Rule>
    <sld:Rule>
      <sld:Name>AS: 2/5 GraduatedColorPolygonRuleList</sld:Name>
      <sld:Title>en{[2.850 - 4.100[}</sld:Title>
      <ogc:Filter>
        <ogc:And>
          <ogc:Not>
            <ogc:Or>
              <ogc:PropertyIsNull>
                <ogc:PropertyName>demstat</ogc:PropertyName>
              </ogc:PropertyIsNull>
              <ogc:PropertyIsEqualTo>
                <ogc:PropertyName>demstat</ogc:PropertyName>
                <ogc:Literal>0.0</ogc:Literal>
              </ogc:PropertyIsEqualTo>
            </ogc:Or>
          </ogc:Not>
          <ogc:PropertyIsBetween>
            <ogc:PropertyName>demstat</ogc:PropertyName>
            <ogc:LowerBoundary>
              <ogc:Literal>2.849999905</ogc:Literal>
            </ogc:LowerBoundary>
            <ogc:UpperBoundary>
              <ogc:Literal>4.099999905</ogc:Literal>
            </ogc:UpperBoundary>
          </ogc:PropertyIsBetween>
        </ogc:And>
      </ogc:Filter>
      <sld:MinScaleDenominator>4.9E-324</sld:MinScaleDenominator>
      <sld:MaxScaleDenominator>1.7976931348623157E308</sld:MaxScaleDenominator>
      <sld:PolygonSymbolizer>
        <sld:Geometry>
          <ogc:PropertyName>the_geom</ogc:PropertyName>
        </sld:Geometry>
        <sld:Fill>
          <sld:CssParameter name="fill">
            <ogc:Literal>#92C5DE</ogc:Literal>
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
            <ogc:Literal>1.0</ogc:Literal>
          </sld:CssParameter>
          <sld:CssParameter name="stroke-dashoffset">
            <ogc:Literal>0.0</ogc:Literal>
          </sld:CssParameter>
        </sld:Stroke>
      </sld:PolygonSymbolizer>
    </sld:Rule>
    <sld:Rule>
      <sld:Name>AS: 3/5 GraduatedColorPolygonRuleList</sld:Name>
      <sld:Title>en{[4.100 - 6.200[}</sld:Title>
      <ogc:Filter>
        <ogc:And>
          <ogc:Not>
            <ogc:Or>
              <ogc:PropertyIsNull>
                <ogc:PropertyName>demstat</ogc:PropertyName>
              </ogc:PropertyIsNull>
              <ogc:PropertyIsEqualTo>
                <ogc:PropertyName>demstat</ogc:PropertyName>
                <ogc:Literal>0.0</ogc:Literal>
              </ogc:PropertyIsEqualTo>
            </ogc:Or>
          </ogc:Not>
          <ogc:PropertyIsBetween>
            <ogc:PropertyName>demstat</ogc:PropertyName>
            <ogc:LowerBoundary>
              <ogc:Literal>4.099999905</ogc:Literal>
            </ogc:LowerBoundary>
            <ogc:UpperBoundary>
              <ogc:Literal>6.199999809</ogc:Literal>
            </ogc:UpperBoundary>
          </ogc:PropertyIsBetween>
        </ogc:And>
      </ogc:Filter>
      <sld:MinScaleDenominator>4.9E-324</sld:MinScaleDenominator>
      <sld:MaxScaleDenominator>1.7976931348623157E308</sld:MaxScaleDenominator>
      <sld:PolygonSymbolizer>
        <sld:Geometry>
          <ogc:PropertyName>the_geom</ogc:PropertyName>
        </sld:Geometry>
        <sld:Fill>
          <sld:CssParameter name="fill">
            <ogc:Literal>#F7F7F7</ogc:Literal>
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
            <ogc:Literal>1.0</ogc:Literal>
          </sld:CssParameter>
          <sld:CssParameter name="stroke-dashoffset">
            <ogc:Literal>0.0</ogc:Literal>
          </sld:CssParameter>
        </sld:Stroke>
      </sld:PolygonSymbolizer>
    </sld:Rule>
    <sld:Rule>
      <sld:Name>AS: 4/5 GraduatedColorPolygonRuleList</sld:Name>
      <sld:Title>en{[6.200 - 7.600[}</sld:Title>
      <ogc:Filter>
        <ogc:And>
          <ogc:Not>
            <ogc:Or>
              <ogc:PropertyIsNull>
                <ogc:PropertyName>demstat</ogc:PropertyName>
              </ogc:PropertyIsNull>
              <ogc:PropertyIsEqualTo>
                <ogc:PropertyName>demstat</ogc:PropertyName>
                <ogc:Literal>0.0</ogc:Literal>
              </ogc:PropertyIsEqualTo>
            </ogc:Or>
          </ogc:Not>
          <ogc:PropertyIsBetween>
            <ogc:PropertyName>demstat</ogc:PropertyName>
            <ogc:LowerBoundary>
              <ogc:Literal>6.199999809</ogc:Literal>
            </ogc:LowerBoundary>
            <ogc:UpperBoundary>
              <ogc:Literal>7.599999905</ogc:Literal>
            </ogc:UpperBoundary>
          </ogc:PropertyIsBetween>
        </ogc:And>
      </ogc:Filter>
      <sld:MinScaleDenominator>4.9E-324</sld:MinScaleDenominator>
      <sld:MaxScaleDenominator>1.7976931348623157E308</sld:MaxScaleDenominator>
      <sld:PolygonSymbolizer>
        <sld:Geometry>
          <ogc:PropertyName>the_geom</ogc:PropertyName>
        </sld:Geometry>
        <sld:Fill>
          <sld:CssParameter name="fill">
            <ogc:Literal>#F4A582</ogc:Literal>
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
            <ogc:Literal>1.0</ogc:Literal>
          </sld:CssParameter>
          <sld:CssParameter name="stroke-dashoffset">
            <ogc:Literal>0.0</ogc:Literal>
          </sld:CssParameter>
        </sld:Stroke>
      </sld:PolygonSymbolizer>
    </sld:Rule>
    <sld:Rule>
      <sld:Name>AS: 5/5 GraduatedColorPolygonRuleList</sld:Name>
      <sld:Title>en{[7.600 - 9.900]}</sld:Title>
      <ogc:Filter>
        <ogc:And>
          <ogc:Not>
            <ogc:Or>
              <ogc:PropertyIsNull>
                <ogc:PropertyName>demstat</ogc:PropertyName>
              </ogc:PropertyIsNull>
              <ogc:PropertyIsEqualTo>
                <ogc:PropertyName>demstat</ogc:PropertyName>
                <ogc:Literal>0.0</ogc:Literal>
              </ogc:PropertyIsEqualTo>
            </ogc:Or>
          </ogc:Not>
          <ogc:PropertyIsBetween>
            <ogc:PropertyName>demstat</ogc:PropertyName>
            <ogc:LowerBoundary>
              <ogc:Literal>7.599999905</ogc:Literal>
            </ogc:LowerBoundary>
            <ogc:UpperBoundary>
              <ogc:Literal>9.899999619</ogc:Literal>
            </ogc:UpperBoundary>
          </ogc:PropertyIsBetween>
        </ogc:And>
      </ogc:Filter>
      <sld:MinScaleDenominator>4.9E-324</sld:MinScaleDenominator>
      <sld:MaxScaleDenominator>1.7976931348623157E308</sld:MaxScaleDenominator>
      <sld:PolygonSymbolizer>
        <sld:Geometry>
          <ogc:PropertyName>the_geom</ogc:PropertyName>
        </sld:Geometry>
        <sld:Fill>
          <sld:CssParameter name="fill">
            <ogc:Literal>#D6604D</ogc:Literal>
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
            <ogc:Literal>1.0</ogc:Literal>
          </sld:CssParameter>
          <sld:CssParameter name="stroke-dashoffset">
            <ogc:Literal>0.0</ogc:Literal>
          </sld:CssParameter>
        </sld:Stroke>
      </sld:PolygonSymbolizer>
    </sld:Rule>
    <sld:Rule>
      <sld:Name>NODATA_RULE_VALUES</sld:Name>
      <sld:Title>de{keine Daten}fr{no data}en{no data}ar{no data}</sld:Title>
      <ogc:Filter>
        <ogc:Or>
          <ogc:PropertyIsNull>
            <ogc:PropertyName>demstat</ogc:PropertyName>
          </ogc:PropertyIsNull>
          <ogc:PropertyIsEqualTo>
            <ogc:PropertyName>demstat</ogc:PropertyName>
            <ogc:Literal>0.0</ogc:Literal>
          </ogc:PropertyIsEqualTo>
        </ogc:Or>
      </ogc:Filter>
      <sld:MinScaleDenominator>4.9E-324</sld:MinScaleDenominator>
      <sld:MaxScaleDenominator>1.7976931348623157E308</sld:MaxScaleDenominator>
      <sld:PolygonSymbolizer>
        <sld:Stroke>
          <sld:CssParameter name="stroke">
            <ogc:Literal>#FFFFFF</ogc:Literal>
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
      </sld:PolygonSymbolizer>
    </sld:Rule>
  </sld:FeatureTypeStyle>
  <sld:FeatureTypeStyle>
    <sld:Name>TEXT_LABEL</sld:Name>
    <sld:FeatureTypeName>Feature</sld:FeatureTypeName>
    <sld:SemanticTypeIdentifier>SemanticType[ANY]</sld:SemanticTypeIdentifier>
    <sld:Rule>
      <sld:Name>Default/all others</sld:Name>
      <ogc:Filter>
        <ogc:PropertyIsEqualTo>
          <ogc:Literal>1</ogc:Literal>
          <ogc:Literal>1</ogc:Literal>
        </ogc:PropertyIsEqualTo>
      </ogc:Filter>
      <sld:MinScaleDenominator>1.0</sld:MinScaleDenominator>
      <sld:MaxScaleDenominator>1.7976931348623157E308</sld:MaxScaleDenominator>
      <sld:TextSymbolizer>
        <sld:Geometry>
          <ogc:PropertyName>the_geom</ogc:PropertyName>
        </sld:Geometry>
        <sld:Label>
          <ogc:PropertyName>CNTRY_NAME</ogc:PropertyName>
        </sld:Label>
        <sld:Font>
          <sld:CssParameter name="font-family">
            <ogc:Literal>Lucida Sans</ogc:Literal>
          </sld:CssParameter>
          <sld:CssParameter name="font-size">
            <ogc:Literal>10.0</ogc:Literal>
          </sld:CssParameter>
          <sld:CssParameter name="font-style">
            <ogc:Literal>normal</ogc:Literal>
          </sld:CssParameter>
          <sld:CssParameter name="font-weight">
            <ogc:Literal>normal</ogc:Literal>
          </sld:CssParameter>
        </sld:Font>
        <sld:LabelPlacement>
          <sld:PointPlacement>
            <sld:AnchorPoint>
              <sld:AnchorPointX>
                <ogc:Literal>.5</ogc:Literal>
              </sld:AnchorPointX>
              <sld:AnchorPointY>
                <ogc:Literal>0.5</ogc:Literal>
              </sld:AnchorPointY>
            </sld:AnchorPoint>
            <sld:Displacement>
              <sld:DisplacementX>
                <ogc:Literal>0.0</ogc:Literal>
              </sld:DisplacementX>
              <sld:DisplacementY>
                <ogc:Literal>0.0</ogc:Literal>
              </sld:DisplacementY>
            </sld:Displacement>
            <sld:Rotation>
              <ogc:Literal>0</ogc:Literal>
            </sld:Rotation>
          </sld:PointPlacement>
        </sld:LabelPlacement>
        <sld:Fill>
          <sld:CssParameter name="fill">
            <ogc:Literal>#000000</ogc:Literal>
          </sld:CssParameter>
          <sld:CssParameter name="fill-opacity">
            <ogc:Literal>1.0</ogc:Literal>
          </sld:CssParameter>
        </sld:Fill>
      </sld:TextSymbolizer>
    </sld:Rule>
  </sld:FeatureTypeStyle>
</sld:UserStyle>
