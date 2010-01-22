/*******************************************************************************
 * Copyright (c) 2009 Stefan A. Krüger.
 * 
 * This file is part of the Geopublisher application - An authoring tool to facilitate the publication and distribution of geoproducts in form of online and/or offline end-user GIS.
 * http://www.geopublishing.org
 * 
 * Geopublisher is part of the Geopublishing Framework hosted at:
 * http://wald.intevation.org/projects/atlas-framework/
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License (license.txt)
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * or try this link: http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Stefan A. Krüger - initial API and implementation
 ******************************************************************************/
package skrueger.creator.gui.datapool;

import java.io.IOException;
import java.util.Set;

import org.geotools.data.DataAccess;
import org.geotools.data.FeatureListener;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.data.QueryCapabilities;
import org.geotools.data.ResourceInfo;
import org.geotools.feature.FeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.GeographicCRS;

import com.vividsolutions.jts.geom.Envelope;

public class TwistedLatLonFeatureSource implements
		FeatureSource<SimpleFeatureType, SimpleFeature> {

	private final FeatureSource<SimpleFeatureType, SimpleFeature> originaleFS;
	private boolean twist = false;

	public TwistedLatLonFeatureSource(
			FeatureSource<SimpleFeatureType, SimpleFeature> originaleFS) {
		this.originaleFS = originaleFS;

		CoordinateReferenceSystem crs = originaleFS.getSchema()
				.getCoordinateReferenceSystem();
		if (crs instanceof GeographicCRS) {
			System.out.println("CRS is GeographicCRS, twist it!");
			twist = true;
		}
	}

	@Override
	public void addFeatureListener(FeatureListener listener) {
		originaleFS.addFeatureListener(listener);
	}

	@Override
	public ReferencedEnvelope getBounds() throws IOException {
		if (twist) {
			Envelope oldEnv = originaleFS.getBounds();
			return new ReferencedEnvelope(oldEnv.getMinY(), oldEnv.getMinX(),
					oldEnv.getMaxY(), oldEnv.getMaxX(), getSchema()
							.getCoordinateReferenceSystem());
		} else
			return originaleFS.getBounds();
	}

	@Override
	public ReferencedEnvelope getBounds(Query query) throws IOException {
		return null;
	}

	@Override
	public int getCount(Query query) throws IOException {
		return originaleFS.getCount(query);
	}

	@Override
	public DataAccess<SimpleFeatureType, SimpleFeature> getDataStore() {
		return originaleFS.getDataStore();
	}

	@Override
	public FeatureCollection<SimpleFeatureType, SimpleFeature> getFeatures()
			throws IOException {
		return originaleFS.getFeatures();
	}

	@Override
	public FeatureCollection<SimpleFeatureType, SimpleFeature> getFeatures(
			Query query) throws IOException {
		return originaleFS.getFeatures(query);
	}

	@Override
	public FeatureCollection<SimpleFeatureType, SimpleFeature> getFeatures(
			Filter filter) throws IOException {
		return originaleFS.getFeatures(filter);
	}

	@Override
	public QueryCapabilities getQueryCapabilities() {
		return originaleFS.getQueryCapabilities();
	}

	@Override
	public SimpleFeatureType getSchema() {
		return originaleFS.getSchema();
	}

	@Override
	public Set getSupportedHints() {
		return originaleFS.getSupportedHints();
	}

	@Override
	public void removeFeatureListener(FeatureListener listener) {
		originaleFS.removeFeatureListener(listener);
	}

	@Override
	public ResourceInfo getInfo() {
		return originaleFS.getInfo();
	}

	@Override
	public Name getName() {
		return originaleFS.getName();
	}

}
