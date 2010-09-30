<?xml version="1.0" encoding="UTF-8"?>
<sld:UserStyle xmlns="http://www.opengis.net/sld" xmlns:sld="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc" xmlns:gml="http://www.opengis.net/gml">
  <sld:Name>AtlasStyler v1.6-SNAPSHOT-r201009281724</sld:Name>
  <sld:Title/>
  <sld:FeatureTypeStyle>
    <sld:Name>QUANTITIES_COLORIZED_POLYGON:VALUE#AV_TEMPER:NORM#null:METHOD#MANUAL:PALETTE#YlOrRd</sld:Name>
    <sld:FeatureTypeName>Point_Char_Dissolve</sld:FeatureTypeName>
    <sld:SemanticTypeIdentifier>SemanticType[ANY]</sld:SemanticTypeIdentifier>
    <sld:Rule>
      <sld:Name>AS: 1/7 GraduatedColorPolygonRuleList</sld:Name>
      <sld:Title>[&lt; -10°C[</sld:Title>
      <ogc:Filter>
        <ogc:And>
          <ogc:Not>
            <ogc:Or>
              <ogc:PropertyIsNull>
                <ogc:PropertyName>AV_TEMPER</ogc:PropertyName>
              </ogc:PropertyIsNull>
            </ogc:Or>
          </ogc:Not>
          <ogc:PropertyIsBetween>
            <ogc:PropertyName>AV_TEMPER</ogc:PropertyName>
            <ogc:LowerBoundary>
              <ogc:Literal>-1000.0</ogc:Literal>
            </ogc:LowerBoundary>
            <ogc:UpperBoundary>
              <ogc:Literal>-10.0</ogc:Literal>
            </ogc:UpperBoundary>
          </ogc:PropertyIsBetween>
        </ogc:And>
      </ogc:Filter>
      <sld:MinScaleDenominator>4.9E-324</sld:MinScaleDenominator>
      <sld:MaxScaleDenominator>1.7976931348623157E308</sld:MaxScaleDenominator>
      <sld:PolygonSymbolizer>
        <sld:Fill>
          <sld:CssParameter name="fill">
            <ogc:Literal>#000001</ogc:Literal>
          </sld:CssParameter>
          <sld:CssParameter name="fill-opacity">
            <ogc:Literal>1.0</ogc:Literal>
          </sld:CssParameter>
        </sld:Fill>
      </sld:PolygonSymbolizer>
    </sld:Rule>
    <sld:Rule>
      <sld:Name>AS: 2/7 GraduatedColorPolygonRuleList</sld:Name>
      <sld:Title>[-10°C - 0°C[</sld:Title>
      <ogc:Filter>
        <ogc:And>
          <ogc:Not>
            <ogc:Or>
              <ogc:PropertyIsNull>
                <ogc:PropertyName>AV_TEMPER</ogc:PropertyName>
              </ogc:PropertyIsNull>
            </ogc:Or>
          </ogc:Not>
          <ogc:PropertyIsBetween>
            <ogc:PropertyName>AV_TEMPER</ogc:PropertyName>
            <ogc:LowerBoundary>
              <ogc:Literal>-10.0</ogc:Literal>
            </ogc:LowerBoundary>
            <ogc:UpperBoundary>
              <ogc:Literal>0.0</ogc:Literal>
            </ogc:UpperBoundary>
          </ogc:PropertyIsBetween>
        </ogc:And>
      </ogc:Filter>
      <sld:MinScaleDenominator>4.9E-324</sld:MinScaleDenominator>
      <sld:MaxScaleDenominator>1.7976931348623157E308</sld:MaxScaleDenominator>
      <sld:PolygonSymbolizer>
        <sld:Fill>
          <sld:CssParameter name="fill">
            <ogc:Literal>#B6FF8F</ogc:Literal>
          </sld:CssParameter>
          <sld:CssParameter name="fill-opacity">
            <ogc:Literal>1.0</ogc:Literal>
          </sld:CssParameter>
        </sld:Fill>
      </sld:PolygonSymbolizer>
    </sld:Rule>
    <sld:Rule>
      <sld:Name>AS: 3/7 GraduatedColorPolygonRuleList</sld:Name>
      <sld:Title>[0°C - 10°C[</sld:Title>
      <ogc:Filter>
        <ogc:And>
          <ogc:Not>
            <ogc:Or>
              <ogc:PropertyIsNull>
                <ogc:PropertyName>AV_TEMPER</ogc:PropertyName>
              </ogc:PropertyIsNull>
            </ogc:Or>
          </ogc:Not>
          <ogc:PropertyIsBetween>
            <ogc:PropertyName>AV_TEMPER</ogc:PropertyName>
            <ogc:LowerBoundary>
              <ogc:Literal>0.0</ogc:Literal>
            </ogc:LowerBoundary>
            <ogc:UpperBoundary>
              <ogc:Literal>10.0</ogc:Literal>
            </ogc:UpperBoundary>
          </ogc:PropertyIsBetween>
        </ogc:And>
      </ogc:Filter>
      <sld:MinScaleDenominator>4.9E-324</sld:MinScaleDenominator>
      <sld:MaxScaleDenominator>1.7976931348623157E308</sld:MaxScaleDenominator>
      <sld:PolygonSymbolizer>
        <sld:Fill>
          <sld:CssParameter name="fill">
            <ogc:Literal>#EEFF4C</ogc:Literal>
          </sld:CssParameter>
          <sld:CssParameter name="fill-opacity">
            <ogc:Literal>1.0</ogc:Literal>
          </sld:CssParameter>
        </sld:Fill>
      </sld:PolygonSymbolizer>
    </sld:Rule>
    <sld:Rule>
      <sld:Name>AS: 4/7 GraduatedColorPolygonRuleList</sld:Name>
      <sld:Title>[10°C - 20°C[</sld:Title>
      <ogc:Filter>
        <ogc:And>
          <ogc:Not>
            <ogc:Or>
              <ogc:PropertyIsNull>
                <ogc:PropertyName>AV_TEMPER</ogc:PropertyName>
              </ogc:PropertyIsNull>
            </ogc:Or>
          </ogc:Not>
          <ogc:PropertyIsBetween>
            <ogc:PropertyName>AV_TEMPER</ogc:PropertyName>
            <ogc:LowerBoundary>
              <ogc:Literal>10.0</ogc:Literal>
            </ogc:LowerBoundary>
            <ogc:UpperBoundary>
              <ogc:Literal>20.0</ogc:Literal>
            </ogc:UpperBoundary>
          </ogc:PropertyIsBetween>
        </ogc:And>
      </ogc:Filter>
      <sld:MinScaleDenominator>4.9E-324</sld:MinScaleDenominator>
      <sld:MaxScaleDenominator>1.7976931348623157E308</sld:MaxScaleDenominator>
      <sld:PolygonSymbolizer>
        <sld:Fill>
          <sld:CssParameter name="fill">
            <ogc:Literal>#FFC800</ogc:Literal>
          </sld:CssParameter>
          <sld:CssParameter name="fill-opacity">
            <ogc:Literal>1.0</ogc:Literal>
          </sld:CssParameter>
        </sld:Fill>
      </sld:PolygonSymbolizer>
    </sld:Rule>
    <sld:Rule>
      <sld:Name>AS: 5/7 GraduatedColorPolygonRuleList</sld:Name>
      <sld:Title>[20°C - 30°C[</sld:Title>
      <ogc:Filter>
        <ogc:And>
          <ogc:Not>
            <ogc:Or>
              <ogc:PropertyIsNull>
                <ogc:PropertyName>AV_TEMPER</ogc:PropertyName>
              </ogc:PropertyIsNull>
            </ogc:Or>
          </ogc:Not>
          <ogc:PropertyIsBetween>
            <ogc:PropertyName>AV_TEMPER</ogc:PropertyName>
            <ogc:LowerBoundary>
              <ogc:Literal>20.0</ogc:Literal>
            </ogc:LowerBoundary>
            <ogc:UpperBoundary>
              <ogc:Literal>30.0</ogc:Literal>
            </ogc:UpperBoundary>
          </ogc:PropertyIsBetween>
        </ogc:And>
      </ogc:Filter>
      <sld:MinScaleDenominator>4.9E-324</sld:MinScaleDenominator>
      <sld:MaxScaleDenominator>1.7976931348623157E308</sld:MaxScaleDenominator>
      <sld:PolygonSymbolizer>
        <sld:Fill>
          <sld:CssParameter name="fill">
            <ogc:Literal>#FF7700</ogc:Literal>
          </sld:CssParameter>
          <sld:CssParameter name="fill-opacity">
            <ogc:Literal>1.0</ogc:Literal>
          </sld:CssParameter>
        </sld:Fill>
      </sld:PolygonSymbolizer>
    </sld:Rule>
    <sld:Rule>
      <sld:Name>AS: 6/7 GraduatedColorPolygonRuleList</sld:Name>
      <sld:Title>[30°C - 40°C[</sld:Title>
      <ogc:Filter>
        <ogc:And>
          <ogc:Not>
            <ogc:Or>
              <ogc:PropertyIsNull>
                <ogc:PropertyName>AV_TEMPER</ogc:PropertyName>
              </ogc:PropertyIsNull>
            </ogc:Or>
          </ogc:Not>
          <ogc:PropertyIsBetween>
            <ogc:PropertyName>AV_TEMPER</ogc:PropertyName>
            <ogc:LowerBoundary>
              <ogc:Literal>30.0</ogc:Literal>
            </ogc:LowerBoundary>
            <ogc:UpperBoundary>
              <ogc:Literal>40.0</ogc:Literal>
            </ogc:UpperBoundary>
          </ogc:PropertyIsBetween>
        </ogc:And>
      </ogc:Filter>
      <sld:MinScaleDenominator>4.9E-324</sld:MinScaleDenominator>
      <sld:MaxScaleDenominator>1.7976931348623157E308</sld:MaxScaleDenominator>
      <sld:PolygonSymbolizer>
        <sld:Fill>
          <sld:CssParameter name="fill">
            <ogc:Literal>#FF0000</ogc:Literal>
          </sld:CssParameter>
          <sld:CssParameter name="fill-opacity">
            <ogc:Literal>1.0</ogc:Literal>
          </sld:CssParameter>
        </sld:Fill>
      </sld:PolygonSymbolizer>
    </sld:Rule>
    <sld:Rule>
      <sld:Name>AS: 7/7 GraduatedColorPolygonRuleList</sld:Name>
      <sld:Title>[&gt; 40°C[</sld:Title>
      <ogc:Filter>
        <ogc:And>
          <ogc:Not>
            <ogc:Or>
              <ogc:PropertyIsNull>
                <ogc:PropertyName>AV_TEMPER</ogc:PropertyName>
              </ogc:PropertyIsNull>
            </ogc:Or>
          </ogc:Not>
          <ogc:PropertyIsBetween>
            <ogc:PropertyName>AV_TEMPER</ogc:PropertyName>
            <ogc:LowerBoundary>
              <ogc:Literal>40.0</ogc:Literal>
            </ogc:LowerBoundary>
            <ogc:UpperBoundary>
              <ogc:Literal>1000.0</ogc:Literal>
            </ogc:UpperBoundary>
          </ogc:PropertyIsBetween>
        </ogc:And>
      </ogc:Filter>
      <sld:MinScaleDenominator>4.9E-324</sld:MinScaleDenominator>
      <sld:MaxScaleDenominator>1.7976931348623157E308</sld:MaxScaleDenominator>
      <sld:PolygonSymbolizer>
        <sld:Fill>
          <sld:CssParameter name="fill">
            <ogc:Literal>#A80000</ogc:Literal>
          </sld:CssParameter>
          <sld:CssParameter name="fill-opacity">
            <ogc:Literal>1.0</ogc:Literal>
          </sld:CssParameter>
        </sld:Fill>
      </sld:PolygonSymbolizer>
    </sld:Rule>
    <sld:Rule>
      <sld:Name>NODATA_RULE</sld:Name>
      <sld:Title>fehlende Daten</sld:Title>
      <ogc:Filter>
        <ogc:Or>
          <ogc:PropertyIsNull>
            <ogc:PropertyName>AV_TEMPER</ogc:PropertyName>
          </ogc:PropertyIsNull>
        </ogc:Or>
      </ogc:Filter>
      <sld:MinScaleDenominator>4.9E-324</sld:MinScaleDenominator>
      <sld:MaxScaleDenominator>1.7976931348623157E308</sld:MaxScaleDenominator>
      <sld:PolygonSymbolizer>
        <sld:Geometry>
          <ogc:PropertyName>the_geom</ogc:PropertyName>
        </sld:Geometry>
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
  </sld:FeatureTypeStyle>
</sld:UserStyle>
