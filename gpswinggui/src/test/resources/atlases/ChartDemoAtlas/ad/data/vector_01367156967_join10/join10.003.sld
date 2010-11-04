<?xml version="1.0" encoding="windows-1252"?>
<sld:UserStyle xmlns="http://www.opengis.net/sld" xmlns:sld="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc" xmlns:gml="http://www.opengis.net/gml">
  <sld:Name>AtlasStyler v1.4, b746 (Mon Feb 22 12:49:00 CET 2010)</sld:Name>
  <sld:Title>wikisquare.de</sld:Title>
  <sld:FeatureTypeStyle>
    <sld:Name>QUANTITIES_COLORIZED_POLYGON:VALUE#meat:NORM#null:METHOD#QUANTILES:PALETTE#OrRd</sld:Name>
    <sld:FeatureTypeName>join10</sld:FeatureTypeName>
    <sld:SemanticTypeIdentifier>SemanticType[ANY]</sld:SemanticTypeIdentifier>
    <sld:Rule>
      <sld:Name>AS: 1/7 GraduatedColorPolygonRuleList</sld:Name>
      <sld:Title>en{[0.000 - 5668.727[}</sld:Title>
      <ogc:Filter>
        <ogc:And>
          <ogc:Not>
            <ogc:Or>
              <ogc:PropertyIsNull>
                <ogc:PropertyName>meat</ogc:PropertyName>
              </ogc:PropertyIsNull>
              <ogc:PropertyIsEqualTo>
                <ogc:PropertyName>meat</ogc:PropertyName>
                <ogc:Literal>0</ogc:Literal>
              </ogc:PropertyIsEqualTo>
            </ogc:Or>
          </ogc:Not>
          <ogc:PropertyIsBetween>
            <ogc:PropertyName>meat</ogc:PropertyName>
            <ogc:LowerBoundary>
              <ogc:Literal>0.0</ogc:Literal>
            </ogc:LowerBoundary>
            <ogc:UpperBoundary>
              <ogc:Literal>5668.727272727273</ogc:Literal>
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
            <ogc:Literal>#FFF7EC</ogc:Literal>
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
      </sld:PolygonSymbolizer>
    </sld:Rule>
    <sld:Rule>
      <sld:Name>AS: 2/7 GraduatedColorPolygonRuleList</sld:Name>
      <sld:Title>en{[5668.727 - 48008.727[}</sld:Title>
      <ogc:Filter>
        <ogc:And>
          <ogc:Not>
            <ogc:Or>
              <ogc:PropertyIsNull>
                <ogc:PropertyName>meat</ogc:PropertyName>
              </ogc:PropertyIsNull>
              <ogc:PropertyIsEqualTo>
                <ogc:PropertyName>meat</ogc:PropertyName>
                <ogc:Literal>0</ogc:Literal>
              </ogc:PropertyIsEqualTo>
            </ogc:Or>
          </ogc:Not>
          <ogc:PropertyIsBetween>
            <ogc:PropertyName>meat</ogc:PropertyName>
            <ogc:LowerBoundary>
              <ogc:Literal>5668.727272727273</ogc:Literal>
            </ogc:LowerBoundary>
            <ogc:UpperBoundary>
              <ogc:Literal>48008.72727272731</ogc:Literal>
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
            <ogc:Literal>#FEE8C8</ogc:Literal>
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
      </sld:PolygonSymbolizer>
    </sld:Rule>
    <sld:Rule>
      <sld:Name>AS: 3/7 GraduatedColorPolygonRuleList</sld:Name>
      <sld:Title>en{[48008.727 - 136189.727[}</sld:Title>
      <ogc:Filter>
        <ogc:And>
          <ogc:Not>
            <ogc:Or>
              <ogc:PropertyIsNull>
                <ogc:PropertyName>meat</ogc:PropertyName>
              </ogc:PropertyIsNull>
              <ogc:PropertyIsEqualTo>
                <ogc:PropertyName>meat</ogc:PropertyName>
                <ogc:Literal>0</ogc:Literal>
              </ogc:PropertyIsEqualTo>
            </ogc:Or>
          </ogc:Not>
          <ogc:PropertyIsBetween>
            <ogc:PropertyName>meat</ogc:PropertyName>
            <ogc:LowerBoundary>
              <ogc:Literal>48008.72727272731</ogc:Literal>
            </ogc:LowerBoundary>
            <ogc:UpperBoundary>
              <ogc:Literal>136189.72727272735</ogc:Literal>
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
            <ogc:Literal>#FDBB84</ogc:Literal>
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
      </sld:PolygonSymbolizer>
    </sld:Rule>
    <sld:Rule>
      <sld:Name>AS: 4/7 GraduatedColorPolygonRuleList</sld:Name>
      <sld:Title>en{[136189.727 - 245030.909[}</sld:Title>
      <ogc:Filter>
        <ogc:And>
          <ogc:Not>
            <ogc:Or>
              <ogc:PropertyIsNull>
                <ogc:PropertyName>meat</ogc:PropertyName>
              </ogc:PropertyIsNull>
              <ogc:PropertyIsEqualTo>
                <ogc:PropertyName>meat</ogc:PropertyName>
                <ogc:Literal>0</ogc:Literal>
              </ogc:PropertyIsEqualTo>
            </ogc:Or>
          </ogc:Not>
          <ogc:PropertyIsBetween>
            <ogc:PropertyName>meat</ogc:PropertyName>
            <ogc:LowerBoundary>
              <ogc:Literal>136189.72727272735</ogc:Literal>
            </ogc:LowerBoundary>
            <ogc:UpperBoundary>
              <ogc:Literal>245030.9090909092</ogc:Literal>
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
            <ogc:Literal>#FC8D59</ogc:Literal>
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
      </sld:PolygonSymbolizer>
    </sld:Rule>
    <sld:Rule>
      <sld:Name>AS: 5/7 GraduatedColorPolygonRuleList</sld:Name>
      <sld:Title>en{[245030.909 - 541643.182[}</sld:Title>
      <ogc:Filter>
        <ogc:And>
          <ogc:Not>
            <ogc:Or>
              <ogc:PropertyIsNull>
                <ogc:PropertyName>meat</ogc:PropertyName>
              </ogc:PropertyIsNull>
              <ogc:PropertyIsEqualTo>
                <ogc:PropertyName>meat</ogc:PropertyName>
                <ogc:Literal>0</ogc:Literal>
              </ogc:PropertyIsEqualTo>
            </ogc:Or>
          </ogc:Not>
          <ogc:PropertyIsBetween>
            <ogc:PropertyName>meat</ogc:PropertyName>
            <ogc:LowerBoundary>
              <ogc:Literal>245030.9090909092</ogc:Literal>
            </ogc:LowerBoundary>
            <ogc:UpperBoundary>
              <ogc:Literal>541643.1818181823</ogc:Literal>
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
            <ogc:Literal>#E34A33</ogc:Literal>
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
      </sld:PolygonSymbolizer>
    </sld:Rule>
    <sld:Rule>
      <sld:Name>AS: 6/7 GraduatedColorPolygonRuleList</sld:Name>
      <sld:Title>en{[541643.182 - 1250952.455[}</sld:Title>
      <ogc:Filter>
        <ogc:And>
          <ogc:Not>
            <ogc:Or>
              <ogc:PropertyIsNull>
                <ogc:PropertyName>meat</ogc:PropertyName>
              </ogc:PropertyIsNull>
              <ogc:PropertyIsEqualTo>
                <ogc:PropertyName>meat</ogc:PropertyName>
                <ogc:Literal>0</ogc:Literal>
              </ogc:PropertyIsEqualTo>
            </ogc:Or>
          </ogc:Not>
          <ogc:PropertyIsBetween>
            <ogc:PropertyName>meat</ogc:PropertyName>
            <ogc:LowerBoundary>
              <ogc:Literal>541643.1818181823</ogc:Literal>
            </ogc:LowerBoundary>
            <ogc:UpperBoundary>
              <ogc:Literal>1250952.4545454553</ogc:Literal>
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
            <ogc:Literal>#B30000</ogc:Literal>
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
      </sld:PolygonSymbolizer>
    </sld:Rule>
    <sld:Rule>
      <sld:Name>AS: 7/7 GraduatedColorPolygonRuleList</sld:Name>
      <sld:Title>en{[1250952.455 - 71822537.000]}</sld:Title>
      <ogc:Filter>
        <ogc:And>
          <ogc:Not>
            <ogc:Or>
              <ogc:PropertyIsNull>
                <ogc:PropertyName>meat</ogc:PropertyName>
              </ogc:PropertyIsNull>
              <ogc:PropertyIsEqualTo>
                <ogc:PropertyName>meat</ogc:PropertyName>
                <ogc:Literal>0</ogc:Literal>
              </ogc:PropertyIsEqualTo>
            </ogc:Or>
          </ogc:Not>
          <ogc:PropertyIsBetween>
            <ogc:PropertyName>meat</ogc:PropertyName>
            <ogc:LowerBoundary>
              <ogc:Literal>1250952.4545454553</ogc:Literal>
            </ogc:LowerBoundary>
            <ogc:UpperBoundary>
              <ogc:Literal>7.1822537E7</ogc:Literal>
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
            <ogc:Literal>#7F0000</ogc:Literal>
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
      </sld:PolygonSymbolizer>
    </sld:Rule>
    <sld:Rule>
      <sld:Name>NODATA_RULE</sld:Name>
      <sld:Title>missing data</sld:Title>
      <ogc:Filter>
        <ogc:Or>
          <ogc:PropertyIsNull>
            <ogc:PropertyName>meat</ogc:PropertyName>
          </ogc:PropertyIsNull>
          <ogc:PropertyIsEqualTo>
            <ogc:PropertyName>meat</ogc:PropertyName>
            <ogc:Literal>0</ogc:Literal>
          </ogc:PropertyIsEqualTo>
        </ogc:Or>
      </ogc:Filter>
      <sld:MinScaleDenominator>4.9E-324</sld:MinScaleDenominator>
      <sld:MaxScaleDenominator>1.7976931348623157E308</sld:MaxScaleDenominator>
      <sld:PolygonSymbolizer>
        <sld:Fill>
          <sld:CssParameter name="fill">
            <ogc:Literal>#FFFFFF</ogc:Literal>
          </sld:CssParameter>
          <sld:CssParameter name="fill-opacity">
            <ogc:Literal>1.0</ogc:Literal>
          </sld:CssParameter>
        </sld:Fill>
        <sld:Stroke>
          <sld:CssParameter name="stroke">
            <ogc:Literal>#C0C0C0</ogc:Literal>
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
  </sld:FeatureTypeStyle>
  <sld:FeatureTypeStyle>
    <sld:Name>TEXT_LABEL</sld:Name>
    <sld:FeatureTypeName>Feature</sld:FeatureTypeName>
    <sld:SemanticTypeIdentifier>SemanticType[ANY]</sld:SemanticTypeIdentifier>
    <sld:Rule>
      <sld:Name>Default/all others</sld:Name>
      <ogc:Filter>
        <ogc:And>
          <ogc:PropertyIsEqualTo>
            <ogc:Literal>ALL_LABEL_CLASSES_ENABLED</ogc:Literal>
            <ogc:Literal>ALL_LABEL_CLASSES_ENABLED</ogc:Literal>
          </ogc:PropertyIsEqualTo>
          <ogc:And>
            <ogc:PropertyIsEqualTo>
              <ogc:Literal>LABEL_CLASS_ENABLED</ogc:Literal>
              <ogc:Literal>LABEL_CLASS_ENABLED</ogc:Literal>
            </ogc:PropertyIsEqualTo>
          </ogc:And>
        </ogc:And>
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
