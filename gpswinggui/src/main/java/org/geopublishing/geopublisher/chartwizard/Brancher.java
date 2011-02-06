/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Tzeggai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Tzeggai - initial API and implementation
 ******************************************************************************/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geopublishing.geopublisher.chartwizard;

import java.util.Map;

import javax.swing.JCheckBox;

import org.netbeans.api.wizard.WizardDisplayer;
import org.netbeans.spi.wizard.Wizard;
import org.netbeans.spi.wizard.WizardBranchController;
import org.netbeans.spi.wizard.WizardPage;
import org.netbeans.spi.wizard.WizardPage.WizardResultProducer;

/**
 * A simple example of a wizard with multiple branch points. It provides the
 * following sequence of pages:
 * 
 * <pre>
 *      /-C1-D1-E1-F1
 *     / 
 *  A-B
 *     \       /-E2a-F2a-G2a
 *      \-C2-D2
 *             \-E2b-F2b-G2b
 * </pre>
 * 
 * Each letter above is a WizardPage subclass with some content on it. Two of
 * them, B and D2 contain checkboxes. Notice that all we do is set the name of
 * the checkbox - this causes it to be listened to automatically, and its
 * selected state put into the data map passed into getWizardForStep().
 * <p/>
 * A branching wizard is really a wrapper for multiple wizards (which may have
 * their own branches, or simply a fixed set of panels. We pass at least an
 * initial panel to the super constructor; when the last page of the initial
 * steps is hit, getWizardForStep() is called to decide what wizard to return
 * for the next sequence.
 * 
 * @author Tim Boudreau
 */
public class Brancher extends WizardBranchController {
	// Probably you really want to create these lazily if they are
	// needed, but the example is clearer this way...</font>
	Class[] c1Sequence = new Class[] { C1.class, D1.class, E1.class, F1.class, };
	// Create a wizard for this sequence of steps
	Wizard c1 = WizardPage.createWizard(c1Sequence, WizardResultProducer.NO_OP);

	// This will be one of the secondary branches
	Class[] e2aSequence = new Class[] { E2a.class, F2a.class, G2a.class, };
	Wizard e2a = WizardPage.createWizard(e2aSequence,
			WizardResultProducer.NO_OP);

	// This will be the other secondary branch
	Class[] e2bSequence = new Class[] { E2b.class, F2b.class, G2b.class, };
	Wizard e2b = WizardPage.createWizard(e2bSequence,
			WizardResultProducer.NO_OP);

	// Now make another branching wizard for our second set of steps

	public static void main(String[] args) {
		Brancher brancher = new Brancher();
		Wizard wizard = brancher.createWizard();
		WizardDisplayer.showWizard(wizard);
	}

	public Brancher() {
		// create the base pages - these are also WizardPage subclasses
		super(new WizardPage[] { new A(), new B() });
	}

	@Override
	public Wizard getWizardForStep(String step, Map data) {
		// log.debug("Get Wizard For Step " + step + " with " + data);
		// The class name is the default ID for instantiated WizardPages
		if ("multibranchdemo.Brancher$B".equals(step)) {
			if (Boolean.TRUE.equals(data.get("foo"))) { // check some data in
				// the map to decide
				return c1;
			} else {
				return new Brancher2().createWizard();
			}
		}
		return null;
	}

	private class Brancher2 extends WizardBranchController {
		Brancher2() {
			super(new WizardPage[] { new C2(), new D2() });
		}

		@Override
		public Wizard getWizardForStep(String step, Map data) {
			if ("multibranchdemo.Brancher$D2".equals(step)) {
				if (Boolean.TRUE.equals(data.get("bar"))) { // check some data
					// in the map to
					// decide
					return e2a;
				} else {
					return e2b;
				}
			}
			return null;
		}
	}

	public static class A extends WizardPage {
		public A() {
			super("Step A");
		}

		public static String getStep() {
			return "A";
		}

		public static String getDescription() {
			return "Step " + getStep();
		}
	}

	public static class B extends WizardPage {
		public B() {
			super("Step B");
			JCheckBox box = new JCheckBox("Follow the C sequence?");
			box.setName("foo");
			add(box);
		}

		public static String getStep() {
			return "B";
		}

		public static String getDescription() {
			return "Step " + getStep();
		}
	}

	public static class C1 extends WizardPage {
		public C1() {
			super("Step C1");
		}

		public static String getStep() {
			return "C1";
		}

		public static String getDescription() {
			return "Step " + getStep();
		}
	}

	public static class D1 extends WizardPage {
		public D1() {
			super("Step D1");
		}

		public static String getStep() {
			return "D1";
		}

		public static String getDescription() {
			return "Step " + getStep();
		}
	}

	public static class E1 extends WizardPage {
		public E1() {
			super("E1");
		}

		public static String getStep() {
			return "E1";
		}

		public static String getDescription() {
			return "Step " + getStep();
		}
	}

	public static class F1 extends WizardPage {
		public F1() {
			super("F1");
		}

		public static String getStep() {
			return "F1";
		}

		public static String getDescription() {
			return "Step " + getStep();
		}
	}

	public static class C2 extends WizardPage {
		public C2() {
			super("C2");
		}

		public static String getStep() {
			return "C2";
		}

		public static String getDescription() {
			return "Step " + getStep();
		}
	}

	public static class D2 extends WizardPage {
		public D2() {
			super("D2");
			JCheckBox box = new JCheckBox("Follow the e2b sequence?");
			box.setName("bar");
			add(box);
		}

		public static String getStep() {
			return "D2";
		}

		public static String getDescription() {
			return "Step " + getStep();
		}
	}

	public static class E2a extends WizardPage {
		public E2a() {
			super("E2a");
		}

		public static String getStep() {
			return "E2a";
		}

		public static String getDescription() {
			return "Step " + getStep();
		}
	}

	public static class F2a extends WizardPage {
		public F2a() {
			super("F2a");
		}

		public static String getStep() {
			return "F2a";
		}

		public static String getDescription() {
			return "Step " + getStep();
		}
	}

	public static class G2a extends WizardPage {
		public G2a() {
			super("G2a");
		}

		public static String getStep() {
			return "G2a";
		}

		public static String getDescription() {
			return "Step " + getStep();
		}
	}

	public static class E2b extends WizardPage {
		public E2b() {
			super("E2b");
		}

		public static String getStep() {
			return "E2b";
		}

		public static String getDescription() {
			return "Step " + getStep();
		}
	}

	public static class F2b extends WizardPage {
		public F2b() {
			super("F2b");
		}

		public static String getStep() {
			return "F2b";
		}

		public static String getDescription() {
			return "Step " + getStep();
		}
	}

	public static class G2b extends WizardPage {
		public G2b() {
			super("G2b");
		}

		public static String getStep() {
			return "G2b";
		}

		public static String getDescription() {
			return "Step " + getStep();
		}
	}
}
