/*
 * JBoss, Home of Professional Open Source.
 * See the COPYRIGHT.txt file distributed with this work for information
 * regarding copyright ownership.  Some portions may be licensed
 * to Red Hat, Inc. under one or more contributor license agreements.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA.
 */
package org.teiid.modeshape.sequencer.ddl;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.modeshape.common.text.ParsingException;
import org.modeshape.jcr.api.JcrConstants;
import org.teiid.modeshape.sequencer.ddl.TeiidDdlConstants.SchemaElementType;
import org.teiid.modeshape.sequencer.ddl.TeiidDdlConstants.TeiidDataType;
import org.teiid.modeshape.sequencer.ddl.TeiidDdlConstants.TeiidNonReservedWord;
import org.teiid.modeshape.sequencer.ddl.TeiidDdlConstants.TeiidReservedWord;
import org.teiid.modeshape.sequencer.ddl.node.AstNode;

/**
 * A test class for {@link CreateTableParser}.
 */
public class CreateTableParserTest extends TeiidDdlTest {

    private CreateTableParser parser;
    private AstNode rootNode;

    @Before
    public void beforeEach() {
        final TeiidDdlParser teiidDdlParser = new TeiidDdlParser();
        this.parser = new CreateTableParser(teiidDdlParser);
        this.rootNode = teiidDdlParser.nodeFactory().node("ddlRootNode");
    }

    /**
     * See Teiid TestDDLParser#testForeignTable()
     */
    @Test
    public void shouldParseForeignTable() {
        final String content = "CREATE FOREIGN TABLE G1(e1 integer primary key, e2 varchar(10) unique,"
                               + "e3 date not null unique, e4 decimal(12,3) options (searchable 'unsearchable'),"
                               + "e5 integer auto_increment INDEX OPTIONS (UUID 'uuid', NAMEINSOURCE 'nis', SELECTABLE 'NO'),"
                               + "e6 varchar index default 'hello')"
                               + "OPTIONS (CARDINALITY 12, UUID 'uuid2', UPDATABLE 'true', FOO 'BAR', ANNOTATION 'Test Table')";
        final AstNode tableNode = this.parser.parse(getTokens(content), this.rootNode);
        assertThat(tableNode.getName(), is("G1"));
        assertMixinType(tableNode, TeiidDdlLexicon.CreateTable.TABLE_STATEMENT);
        assertProperty(tableNode, TeiidDdlLexicon.SchemaElement.TYPE, SchemaElementType.FOREIGN.toDdl());

        // columns
        assertThat(tableNode.getChildren(TeiidDdlLexicon.CreateTable.TABLE_ELEMENT).size(), is(6));

        { // column e1
            assertThat(tableNode.childrenWithName("e1").size(), is(1));
            final AstNode e1 = tableNode.childrenWithName("e1").get(0);
            assertProperty(e1, StandardDdlLexicon.DATATYPE_NAME, TeiidDataType.INTEGER.toDdl());
            assertProperty(e1, TeiidDdlLexicon.CreateTable.AUTO_INCREMENT, false);
            assertProperty(e1, StandardDdlLexicon.NULLABLE, DdlConstants.NULL);
        }

        { // column e2
            assertThat(tableNode.childrenWithName("e2").size(), is(1));
            final AstNode e2 = tableNode.childrenWithName("e2").get(0);
            assertProperty(e2, StandardDdlLexicon.DATATYPE_NAME, TeiidDataType.VARCHAR.toDdl());
            assertProperty(e2, StandardDdlLexicon.DATATYPE_LENGTH, 10L);
            assertProperty(e2, TeiidDdlLexicon.CreateTable.AUTO_INCREMENT, false);
            assertProperty(e2, StandardDdlLexicon.NULLABLE, "NULL");
        }

        { // column e3
            assertThat(tableNode.childrenWithName("e3").size(), is(1));
            final AstNode e3 = tableNode.childrenWithName("e3").get(0);
            assertProperty(e3, StandardDdlLexicon.DATATYPE_NAME, TeiidDataType.DATE.toDdl());
            assertProperty(e3, TeiidDdlLexicon.CreateTable.AUTO_INCREMENT, false);
            assertProperty(e3, StandardDdlLexicon.NULLABLE, "NOT NULL");
        }

        { // column e4
            assertThat(tableNode.childrenWithName("e4").size(), is(1));
            final AstNode e4 = tableNode.childrenWithName("e4").get(0);
            assertProperty(e4, StandardDdlLexicon.DATATYPE_NAME, TeiidDataType.DECIMAL.toDdl());
            assertProperty(e4, StandardDdlLexicon.DATATYPE_PRECISION, 12);
            assertProperty(e4, StandardDdlLexicon.DATATYPE_SCALE, 3);
            assertProperty(e4, TeiidDdlLexicon.CreateTable.AUTO_INCREMENT, false);
            assertProperty(e4, StandardDdlLexicon.NULLABLE, "NULL");

            // options
            assertThat(e4.getChildren(StandardDdlLexicon.TYPE_STATEMENT_OPTION).size(), is(1));
            assertThat(e4.getChildren(StandardDdlLexicon.TYPE_STATEMENT_OPTION).get(0).getName(), is("searchable"));
            assertProperty(e4.getChildren(StandardDdlLexicon.TYPE_STATEMENT_OPTION).get(0),
                           StandardDdlLexicon.VALUE,
                           "unsearchable");
        }

        { // column e5
            assertThat(tableNode.childrenWithName("e5").size(), is(1));
            final AstNode e5 = tableNode.childrenWithName("e5").get(0);
            assertProperty(e5, StandardDdlLexicon.DATATYPE_NAME, TeiidDataType.INTEGER.toDdl());
            assertProperty(e5, TeiidDdlLexicon.CreateTable.AUTO_INCREMENT, true);
            assertProperty(e5, StandardDdlLexicon.NULLABLE, "NULL");

            // options
            assertThat(e5.getChildren(StandardDdlLexicon.TYPE_STATEMENT_OPTION).size(), is(3));
            assertThat(e5.childrenWithName("UUID").size(), is(1));
            assertProperty(e5.childrenWithName("UUID").get(0), StandardDdlLexicon.VALUE, "uuid");
            assertThat(e5.childrenWithName("NAMEINSOURCE").size(), is(1));
            assertProperty(e5.childrenWithName("NAMEINSOURCE").get(0), StandardDdlLexicon.VALUE, "nis");
            assertThat(e5.childrenWithName("SELECTABLE").size(), is(1));
            assertProperty(e5.childrenWithName("SELECTABLE").get(0), StandardDdlLexicon.VALUE, "NO");
        }

        { // column e6
            assertThat(tableNode.childrenWithName("e6").size(), is(1));
            final AstNode e6 = tableNode.childrenWithName("e6").get(0);
            assertProperty(e6, StandardDdlLexicon.DATATYPE_NAME, TeiidDataType.VARCHAR.toDdl());
            assertProperty(e6, TeiidDdlLexicon.CreateTable.AUTO_INCREMENT, false);
            assertProperty(e6, StandardDdlLexicon.NULLABLE, "NULL");
            assertProperty(e6, StandardDdlLexicon.DEFAULT_VALUE, "hello");
        }

        // constraints
        assertThat(tableNode.getChildren(TeiidDdlLexicon.Constraint.TABLE_ELEMENT).size(), is(3));

        { // e1 primary key
            assertThat(tableNode.childrenWithName(DdlConstants.PRIMARY_KEY).size(), is(1));
            final Object temp = tableNode.childrenWithName(DdlConstants.PRIMARY_KEY).get(0).getProperty(TeiidDdlLexicon.Constraint.REFERENCES);
            assertThat(temp, is(instanceOf(List.class)));

            @SuppressWarnings( "unchecked" )
            final List<AstNode> references = (List<AstNode>)temp;
            assertThat(references.size(), is(1));
            assertThat(references.get(0), is(tableNode.childrenWithName("e1").get(0)));
        }

        { // e2, e3 unique constraints
            final List<AstNode> uniques = tableNode.childrenWithName(DdlConstants.UNIQUE);
            assertThat(uniques.size(), is(2));

            final Object temp1 = uniques.get(0).getProperty(TeiidDdlLexicon.Constraint.REFERENCES);
            assertThat(temp1, is(instanceOf(List.class)));

            @SuppressWarnings( "unchecked" )
            final List<AstNode> references1 = (List<AstNode>)temp1;
            assertThat(references1.size(), is(1));

            final Object temp2 = uniques.get(1).getProperty(TeiidDdlLexicon.Constraint.REFERENCES);
            assertThat(temp2, is(instanceOf(List.class)));

            @SuppressWarnings( "unchecked" )
            final List<AstNode> references2 = (List<AstNode>)temp2;
            assertThat(references2.size(), is(1));

            if (references1.get(0).equals(tableNode.childrenWithName("e2").get(0))) {
                assertThat(references2, hasItem(tableNode.childrenWithName("e3").get(0)));
            } else if (references1.get(0).equals(tableNode.childrenWithName("e3").get(0))) {
                assertThat(references2, hasItem(tableNode.childrenWithName("e2").get(0)));
            } else {
                fail("unique constraints for column 'e1' and 'e2' not found");
            }
        }

        assertThat(tableNode.getChildren(TeiidDdlLexicon.Constraint.INDEX_CONSTRAINT).size(), is(2));

        // options
        assertThat(tableNode.getChildren(StandardDdlLexicon.TYPE_STATEMENT_OPTION).size(), is(5));
        assertThat(tableNode.childrenWithName("CARDINALITY").size(), is(1));
        assertProperty(tableNode.childrenWithName("CARDINALITY").get(0), StandardDdlLexicon.VALUE, "12");
        assertThat(tableNode.childrenWithName("UUID").size(), is(1));
        assertProperty(tableNode.childrenWithName("UUID").get(0), StandardDdlLexicon.VALUE, "uuid2");
        assertThat(tableNode.childrenWithName("UPDATABLE").size(), is(1));
        assertProperty(tableNode.childrenWithName("UPDATABLE").get(0), StandardDdlLexicon.VALUE, "true");
        assertThat(tableNode.childrenWithName("FOO").size(), is(1));
        assertProperty(tableNode.childrenWithName("FOO").get(0), StandardDdlLexicon.VALUE, "BAR");
        assertThat(tableNode.childrenWithName("ANNOTATION").size(), is(1));
        assertProperty(tableNode.childrenWithName("ANNOTATION").get(0), StandardDdlLexicon.VALUE, "Test Table");
    }

    @Test
    public void shouldParseGlobalTemporaryTable() {
        final String content = "CREATE GLOBAL TEMPORARY TABLE TEMP1 ("
                                + "e1 string,"
                                + "e2 SERIAL,"
                                + "PRIMARY KEY(e1))";
        final AstNode tableNode = this.parser.parse(getTokens(content), this.rootNode);
        assertThat(tableNode.getName(), is("TEMP1"));
        assertMixinType(tableNode, TeiidDdlLexicon.CreateTable.GLOBAL_TEMP_TABLE_STATEMENT);
        assertProperty(tableNode, TeiidDdlLexicon.SchemaElement.TYPE, SchemaElementType.VIRTUAL.toDdl());

        // columns
        assertThat(tableNode.getChildren(TeiidDdlLexicon.CreateTable.TABLE_ELEMENT).size(), is(2));

        { // column x
            assertThat(tableNode.childrenWithName("e1").size(), is(1));
            final AstNode e1 = tableNode.childrenWithName("e1").get(0);
            assertProperty(e1, StandardDdlLexicon.DATATYPE_NAME, TeiidDataType.STRING.toDdl());
            assertProperty(e1, TeiidDdlLexicon.CreateTable.AUTO_INCREMENT, false);
            assertProperty(e1, StandardDdlLexicon.NULLABLE, DdlConstants.NULL);
        }

        { // column y
            assertThat(tableNode.childrenWithName("e2").size(), is(1));
            final AstNode e2 = tableNode.childrenWithName("e2").get(0);
            //
            // SERIAL is a pseudo data type that converts to non-null auto-increment integer
            //
            assertProperty(e2, StandardDdlLexicon.DATATYPE_NAME, TeiidDataType.INTEGER.toDdl());
            assertProperty(e2, TeiidDdlLexicon.CreateTable.AUTO_INCREMENT, true);
            assertProperty(e2, StandardDdlLexicon.NULLABLE, "NOT NULL");
        }

        // constraints
        assertThat(tableNode.getChildren(TeiidDdlLexicon.Constraint.TABLE_ELEMENT).size(), is(1));

        { // x primary key
            assertThat(tableNode.childrenWithName(CreateTableParser.PRIMARY_KEY_PREFIX + "1").size(), is(1));
            final Object temp = tableNode.childrenWithName(CreateTableParser.PRIMARY_KEY_PREFIX + "1").get(0).getProperty(TeiidDdlLexicon.Constraint.REFERENCES);
            assertThat(temp, is(instanceOf(List.class)));

            @SuppressWarnings( "unchecked" )
            final List<AstNode> references = (List<AstNode>)temp;
            assertThat(references.size(), is(1));
            assertThat(references.get(0), is(tableNode.childrenWithName("e1").get(0)));
        }
    }

    @Test
    public void globalTemporaryTableDoesNotSupportAS() {
        final String content = "CREATE GLOBAL TEMPORARY TABLE TEMP1 ("
                                + "e1 string,"
                                + "e2 SERIAL,"
                                + "PRIMARY KEY(e1))"
                                + "AS SELECT * FROM TABLE1;";
        try {
            this.parser.parse(getTokens(content), this.rootNode);
            fail("Should not parse - GLOBAL TEMPORARY TABLES do not have AS statements");
        } catch (Exception ex) {
            assertThat(ex.getMessage(), containsString(
                  "Unparsable create table statement: CREATE GLOBAL TEMPORARY TABLES do not have AS statements"));
        }
    }

    /**
     * See Teiid TestDDLParser#testDuplicatePrimarykey()
     */
    @Test
    public void shouldParseDuplicatePrimaryKey() {
        final String content = "CREATE FOREIGN TABLE G1( e1 integer primary key, e2 varchar primary key)";
        final AstNode tableNode = this.parser.parse(getTokens(content), this.rootNode);
        assertThat(tableNode.getName(), is("G1"));
        assertMixinType(tableNode, TeiidDdlLexicon.CreateTable.TABLE_STATEMENT);
        assertProperty(tableNode, TeiidDdlLexicon.SchemaElement.TYPE, SchemaElementType.FOREIGN.toDdl());

        // columns
        assertThat(tableNode.getChildren(TeiidDdlLexicon.Constraint.TABLE_ELEMENT).size(), is(2));
        assertThat(tableNode.childrenWithName("e1").size(), is(1));
        assertProperty(tableNode.childrenWithName("e1").get(0), StandardDdlLexicon.DATATYPE_NAME, TeiidDataType.INTEGER.toDdl());
        assertThat(tableNode.childrenWithName("e2").size(), is(1));
        assertProperty(tableNode.childrenWithName("e2").get(0), StandardDdlLexicon.DATATYPE_NAME, TeiidDataType.VARCHAR.toDdl());

        // constraints
        assertThat(tableNode.getChildren(TeiidDdlLexicon.Constraint.INDEX_CONSTRAINT).size(), is(0));
    }

    /**
     * See Teiid TestDDLParser#testAutoIncrementPrimarykey()
     */
    @Test
    public void shouldParseAutoIncrementPrimaryKey() {
        final String content = "CREATE FOREIGN TABLE G1( e1 integer auto_increment primary key, e2 varchar)";
        final AstNode tableNode = this.parser.parse(getTokens(content), this.rootNode);
        assertThat(tableNode.getName(), is("G1"));
        assertMixinType(tableNode, TeiidDdlLexicon.CreateTable.TABLE_STATEMENT);
        assertProperty(tableNode, TeiidDdlLexicon.SchemaElement.TYPE, SchemaElementType.FOREIGN.toDdl());

        // columns
        assertThat(tableNode.getChildren(TeiidDdlLexicon.CreateTable.TABLE_ELEMENT).size(), is(2));

        { // column e1
            assertThat(tableNode.childrenWithName("e1").size(), is(1));
            final AstNode e1 = tableNode.childrenWithName("e1").get(0);
            assertProperty(e1, StandardDdlLexicon.DATATYPE_NAME, TeiidDataType.INTEGER.toDdl());
            assertProperty(e1, TeiidDdlLexicon.CreateTable.AUTO_INCREMENT, true);
            assertProperty(e1, StandardDdlLexicon.NULLABLE, DdlConstants.NULL);
        }

        { // column e2
            assertThat(tableNode.childrenWithName("e2").size(), is(1));
            final AstNode e2 = tableNode.childrenWithName("e2").get(0);
            assertProperty(e2, StandardDdlLexicon.DATATYPE_NAME, TeiidDataType.VARCHAR.toDdl());
            assertProperty(e2, TeiidDdlLexicon.CreateTable.AUTO_INCREMENT, false);
            assertProperty(e2, StandardDdlLexicon.NULLABLE, "NULL");
        }

        // constraints
        assertThat(tableNode.getChildren(TeiidDdlLexicon.Constraint.TABLE_ELEMENT).size(), is(1));
        assertThat(tableNode.getChildren(TeiidDdlLexicon.Constraint.INDEX_CONSTRAINT).size(), is(0));
    }

    /**
     * See Teiid TestDDLParser#testUDT()
     */
    @Test
    public void shouldParseUdt() {
        final String content = "CREATE FOREIGN TABLE G1( e1 integer, e2 varchar OPTIONS (UDT 'NMTOKENS(12,13,14)'))";
        final AstNode tableNode = this.parser.parse(getTokens(content), this.rootNode);
        assertThat(tableNode.getName(), is("G1"));
        assertMixinType(tableNode, TeiidDdlLexicon.CreateTable.TABLE_STATEMENT);
        assertProperty(tableNode, TeiidDdlLexicon.SchemaElement.TYPE, SchemaElementType.FOREIGN.toDdl());

        // columns
        assertThat(tableNode.getChildren(TeiidDdlLexicon.CreateTable.TABLE_ELEMENT).size(), is(2));

        { // column e1
            assertThat(tableNode.childrenWithName("e1").size(), is(1));
            final AstNode e1 = tableNode.childrenWithName("e1").get(0);
            assertProperty(e1, StandardDdlLexicon.DATATYPE_NAME, TeiidDataType.INTEGER.toDdl());
            assertProperty(e1, TeiidDdlLexicon.CreateTable.AUTO_INCREMENT, false);
            assertProperty(e1, StandardDdlLexicon.NULLABLE, DdlConstants.NULL);
        }

        { // column e2
            assertThat(tableNode.childrenWithName("e2").size(), is(1));
            final AstNode e2 = tableNode.childrenWithName("e2").get(0);
            assertProperty(e2, StandardDdlLexicon.DATATYPE_NAME, TeiidDataType.VARCHAR.toDdl());
            assertProperty(e2, TeiidDdlLexicon.CreateTable.AUTO_INCREMENT, false);
            assertProperty(e2, StandardDdlLexicon.NULLABLE, "NULL");
        }

        // constraints
        assertThat(tableNode.getChildren(TeiidDdlLexicon.Constraint.TABLE_ELEMENT).size(), is(0));
        assertThat(tableNode.getChildren(TeiidDdlLexicon.Constraint.INDEX_CONSTRAINT).size(), is(0));
    }

    /**
     * See Teiid TestDDLParser#testFBI()
     */
    @Test
    public void shouldParseFunctionIndex() {
        final String content = "CREATE FOREIGN TABLE G1(e1 integer, e2 varchar, CONSTRAINT fbi INDEX (UPPER(e2)))";
        final AstNode tableNode = this.parser.parse(getTokens(content), this.rootNode);
        assertThat(tableNode.getName(), is("G1"));
        assertMixinType(tableNode, TeiidDdlLexicon.CreateTable.TABLE_STATEMENT);
        assertProperty(tableNode, TeiidDdlLexicon.SchemaElement.TYPE, SchemaElementType.FOREIGN.toString());

        // columns
        assertThat(tableNode.getChildren(TeiidDdlLexicon.CreateTable.TABLE_ELEMENT).size(), is(2));

        { // column e1
            assertThat(tableNode.childrenWithName("e1").size(), is(1));
            final AstNode e1 = tableNode.childrenWithName("e1").get(0);
            assertProperty(e1, StandardDdlLexicon.DATATYPE_NAME, TeiidDataType.INTEGER.toDdl());
            assertProperty(e1, TeiidDdlLexicon.CreateTable.AUTO_INCREMENT, false);
            assertProperty(e1, StandardDdlLexicon.NULLABLE, DdlConstants.NULL);
        }

        { // column e2
            assertThat(tableNode.childrenWithName("e2").size(), is(1));
            final AstNode e2 = tableNode.childrenWithName("e2").get(0);
            assertProperty(e2, StandardDdlLexicon.DATATYPE_NAME, TeiidDataType.VARCHAR.toDdl());
            assertProperty(e2, TeiidDdlLexicon.CreateTable.AUTO_INCREMENT, false);
            assertProperty(e2, StandardDdlLexicon.NULLABLE, "NULL");
        }

        // there are no table element constraints
        assertThat(tableNode.getChildren(TeiidDdlLexicon.Constraint.TABLE_ELEMENT).size(), is(0));

        { // index constraint
            assertThat(tableNode.getChildren(TeiidDdlLexicon.Constraint.INDEX_CONSTRAINT).size(), is(1));
            final AstNode indexNode = tableNode.getChildren(TeiidDdlLexicon.Constraint.INDEX_CONSTRAINT).get(0);
            assertThat(indexNode.getName(), is("fbi"));
            assertProperty(indexNode, TeiidDdlLexicon.Constraint.EXPRESSION, "UPPER(e2)");

            final Object temp = indexNode.getProperty(TeiidDdlLexicon.Constraint.REFERENCES);
            assertThat(temp, is(instanceOf(List.class)));

            @SuppressWarnings( "unchecked" )
            final List<AstNode> references = (List<AstNode>)temp;
            assertThat(references.size(), is(1));
            assertThat(references.get(0), is(tableNode.childrenWithName("e2").get(0)));
        }
    }

    /**
     * See Teiid TestDDLParser#testMultiKeyPK()
     */
    @Test
    public void shouldParseMultiKeyPrimaryKey() {
        final String content = "CREATE FOREIGN TABLE G1( e1 integer, e2 varchar, e3 date, PRIMARY KEY (e1, e2))";
        final AstNode tableNode = this.parser.parse(getTokens(content), this.rootNode);
        assertThat(tableNode.getName(), is("G1"));
        assertMixinType(tableNode, TeiidDdlLexicon.CreateTable.TABLE_STATEMENT);
        assertProperty(tableNode, TeiidDdlLexicon.SchemaElement.TYPE, SchemaElementType.FOREIGN.toDdl());

        // columns
        assertThat(tableNode.getChildren(TeiidDdlLexicon.CreateTable.TABLE_ELEMENT).size(), is(3));

        { // column e1
            assertThat(tableNode.childrenWithName("e1").size(), is(1));
            final AstNode e1 = tableNode.childrenWithName("e1").get(0);
            assertProperty(e1, StandardDdlLexicon.DATATYPE_NAME, TeiidDataType.INTEGER.toDdl());
            assertProperty(e1, TeiidDdlLexicon.CreateTable.AUTO_INCREMENT, false);
            assertProperty(e1, StandardDdlLexicon.NULLABLE, DdlConstants.NULL);
        }

        { // column e2
            assertThat(tableNode.childrenWithName("e2").size(), is(1));
            final AstNode e2 = tableNode.childrenWithName("e2").get(0);
            assertProperty(e2, StandardDdlLexicon.DATATYPE_NAME, TeiidDataType.VARCHAR.toDdl());
            assertProperty(e2, TeiidDdlLexicon.CreateTable.AUTO_INCREMENT, false);
            assertProperty(e2, StandardDdlLexicon.NULLABLE, "NULL");
        }

        { // column e3
            assertThat(tableNode.childrenWithName("e3").size(), is(1));
            final AstNode e3 = tableNode.childrenWithName("e3").get(0);
            assertProperty(e3, StandardDdlLexicon.DATATYPE_NAME, TeiidDataType.DATE.toDdl());
            assertProperty(e3, TeiidDdlLexicon.CreateTable.AUTO_INCREMENT, false);
            assertProperty(e3, StandardDdlLexicon.NULLABLE, "NULL");
        }

        // constraints
        assertThat(tableNode.getChildren(TeiidDdlLexicon.Constraint.TABLE_ELEMENT).size(), is(1));
        assertThat(tableNode.getChildren(TeiidDdlLexicon.Constraint.INDEX_CONSTRAINT).size(), is(0));
    }

    /**
     * See Teiid TestDDLParser#testOptionsKey()
     */
    @Test
    public void shouldParseOptionsKey() {
        final String content = "CREATE FOREIGN TABLE G1( e1 integer, e2 varchar, e3 date, UNIQUE (e1) OPTIONS (CUSTOM_PROP 'VALUE'))";
        final AstNode tableNode = this.parser.parse(getTokens(content), this.rootNode);
        assertThat(tableNode.getName(), is("G1"));
        assertMixinType(tableNode, TeiidDdlLexicon.CreateTable.TABLE_STATEMENT);
        assertProperty(tableNode, TeiidDdlLexicon.SchemaElement.TYPE, SchemaElementType.FOREIGN.toDdl());

        // columns
        assertThat(tableNode.getChildren(TeiidDdlLexicon.CreateTable.TABLE_ELEMENT).size(), is(3));

        { // column e1
            assertThat(tableNode.childrenWithName("e1").size(), is(1));
            final AstNode e1 = tableNode.childrenWithName("e1").get(0);
            assertProperty(e1, StandardDdlLexicon.DATATYPE_NAME, TeiidDataType.INTEGER.toDdl());
            assertProperty(e1, TeiidDdlLexicon.CreateTable.AUTO_INCREMENT, false);
            assertProperty(e1, StandardDdlLexicon.NULLABLE, DdlConstants.NULL);
        }

        { // column e2
            assertThat(tableNode.childrenWithName("e2").size(), is(1));
            final AstNode e2 = tableNode.childrenWithName("e2").get(0);
            assertProperty(e2, StandardDdlLexicon.DATATYPE_NAME, TeiidDataType.VARCHAR.toDdl());
            assertProperty(e2, TeiidDdlLexicon.CreateTable.AUTO_INCREMENT, false);
            assertProperty(e2, StandardDdlLexicon.NULLABLE, "NULL");
        }

        { // column e3
            assertThat(tableNode.childrenWithName("e3").size(), is(1));
            final AstNode e3 = tableNode.childrenWithName("e3").get(0);
            assertProperty(e3, StandardDdlLexicon.DATATYPE_NAME, TeiidDataType.DATE.toDdl());
            assertProperty(e3, TeiidDdlLexicon.CreateTable.AUTO_INCREMENT, false);
            assertProperty(e3, StandardDdlLexicon.NULLABLE, "NULL");
        }

        // constraints
        assertThat(tableNode.getChildren(TeiidDdlLexicon.Constraint.TABLE_ELEMENT).size(), is(1));
        assertThat(tableNode.getChildren(TeiidDdlLexicon.Constraint.INDEX_CONSTRAINT).size(), is(0));
    }

    /**
     * See Teiid TestDDLParser#testConstraints()
     */
    @Test
    public void shouldParseConstraints() {
        final String content = "CREATE FOREIGN TABLE G1( e1 integer, e2 varchar, e3 date, PRIMARY KEY (e1, e2), INDEX(e2, e3), ACCESSPATTERN(e1), UNIQUE(e1), ACCESSPATTERN(e2, e3))";
        final AstNode tableNode = this.parser.parse(getTokens(content), this.rootNode);
        assertThat(tableNode.getName(), is("G1"));
        assertMixinType(tableNode, TeiidDdlLexicon.CreateTable.TABLE_STATEMENT);
        assertProperty(tableNode, TeiidDdlLexicon.SchemaElement.TYPE, SchemaElementType.FOREIGN.toDdl());

        // columns
        assertThat(tableNode.getChildren(TeiidDdlLexicon.CreateTable.TABLE_ELEMENT).size(), is(3));

        { // column e1
            assertThat(tableNode.childrenWithName("e1").size(), is(1));
            final AstNode e1 = tableNode.childrenWithName("e1").get(0);
            assertProperty(e1, StandardDdlLexicon.DATATYPE_NAME, TeiidDataType.INTEGER.toDdl());
            assertProperty(e1, TeiidDdlLexicon.CreateTable.AUTO_INCREMENT, false);
            assertProperty(e1, StandardDdlLexicon.NULLABLE, DdlConstants.NULL);
        }

        { // column e2
            assertThat(tableNode.childrenWithName("e2").size(), is(1));
            final AstNode e2 = tableNode.childrenWithName("e2").get(0);
            assertProperty(e2, StandardDdlLexicon.DATATYPE_NAME, TeiidDataType.VARCHAR.toDdl());
            assertProperty(e2, TeiidDdlLexicon.CreateTable.AUTO_INCREMENT, false);
            assertProperty(e2, StandardDdlLexicon.NULLABLE, "NULL");
        }

        { // column e3
            assertThat(tableNode.childrenWithName("e3").size(), is(1));
            final AstNode e3 = tableNode.childrenWithName("e3").get(0);
            assertProperty(e3, StandardDdlLexicon.DATATYPE_NAME, TeiidDataType.DATE.toDdl());
            assertProperty(e3, TeiidDdlLexicon.CreateTable.AUTO_INCREMENT, false);
            assertProperty(e3, StandardDdlLexicon.NULLABLE, "NULL");
        }

        { // table element constraints (1 primary key, 2 access patterns, 1 unique)
            final List<AstNode> tableElementConstraints = tableNode.getChildren(TeiidDdlLexicon.Constraint.TABLE_ELEMENT);
            assertThat(tableElementConstraints.size(), is(4));

            { // primary key
                final AstNode constraintNode = tableElementConstraints.get(0);
                final Object temp = constraintNode.getProperty(TeiidDdlLexicon.Constraint.REFERENCES);
                assertThat(temp, is(instanceOf(List.class)));

                @SuppressWarnings( "unchecked" )
                final List<AstNode> references = (List<AstNode>)temp;
                assertThat(references.size(), is(2));
                assertThat(references, hasItems(tableNode.childrenWithName("e1").get(0), tableNode.childrenWithName("e2").get(0)));
            }

            { // access pattern
                final AstNode constraintNode = tableElementConstraints.get(1);
                final Object temp = constraintNode.getProperty(TeiidDdlLexicon.Constraint.REFERENCES);
                assertThat(temp, is(instanceOf(List.class)));

                @SuppressWarnings( "unchecked" )
                final List<AstNode> references = (List<AstNode>)temp;
                assertThat(references.size(), is(1));
                assertThat(references, hasItems(tableNode.childrenWithName("e1").get(0)));
            }

            { // unique
                final AstNode constraintNode = tableElementConstraints.get(2);
                final Object temp = constraintNode.getProperty(TeiidDdlLexicon.Constraint.REFERENCES);
                assertThat(temp, is(instanceOf(List.class)));

                @SuppressWarnings( "unchecked" )
                final List<AstNode> references = (List<AstNode>)temp;
                assertThat(references.size(), is(1));
                assertThat(references, hasItems(tableNode.childrenWithName("e1").get(0)));
            }

            { // access pattern
                final AstNode constraintNode = tableElementConstraints.get(3);
                final Object temp = constraintNode.getProperty(TeiidDdlLexicon.Constraint.REFERENCES);
                assertThat(temp, is(instanceOf(List.class)));

                @SuppressWarnings( "unchecked" )
                final List<AstNode> references = (List<AstNode>)temp;
                assertThat(references.size(), is(2));
                assertThat(references, hasItems(tableNode.childrenWithName("e2").get(0), tableNode.childrenWithName("e3").get(0)));
            }
        }

        { // index constraint
            assertThat(tableNode.getChildren(TeiidDdlLexicon.Constraint.INDEX_CONSTRAINT).size(), is(1));
            final AstNode indexNode = tableNode.getChildren(TeiidDdlLexicon.Constraint.INDEX_CONSTRAINT).get(0);
            assertProperty(indexNode, TeiidDdlLexicon.Constraint.EXPRESSION, "e2, e3");
            assertProperty(indexNode, TeiidDdlLexicon.Constraint.TYPE, "INDEX");

            final Object temp = indexNode.getProperty(TeiidDdlLexicon.Constraint.REFERENCES);
            assertThat(temp, is(instanceOf(List.class)));

            @SuppressWarnings( "unchecked" )
            final List<AstNode> references = (List<AstNode>)temp;
            assertThat(references.size(), is(2));
            assertThat(references, hasItems(tableNode.childrenWithName("e2").get(0), tableNode.childrenWithName("e3").get(0)));
        }
    }

    /**
     * See Teiid TestDDLParser#testConstraints2()
     */
    @Test
    public void shouldParseConstraints2() {
        final String content = "CREATE FOREIGN TABLE G1( e1 integer, e2 varchar, e3 date, ACCESSPATTERN(e1), UNIQUE(e1), ACCESSPATTERN(e2, e3))";
        final AstNode tableNode = this.parser.parse(getTokens(content), this.rootNode);
        assertThat(tableNode.getName(), is("G1"));
        assertMixinType(tableNode, TeiidDdlLexicon.CreateTable.TABLE_STATEMENT);
        assertProperty(tableNode, TeiidDdlLexicon.SchemaElement.TYPE, SchemaElementType.FOREIGN.toDdl());

        // columns
        assertThat(tableNode.getChildren(TeiidDdlLexicon.CreateTable.TABLE_ELEMENT).size(), is(3));

        { // column e1
            assertThat(tableNode.childrenWithName("e1").size(), is(1));
            final AstNode e1 = tableNode.childrenWithName("e1").get(0);
            assertProperty(e1, StandardDdlLexicon.DATATYPE_NAME, TeiidDataType.INTEGER.toDdl());
            assertProperty(e1, TeiidDdlLexicon.CreateTable.AUTO_INCREMENT, false);
            assertProperty(e1, StandardDdlLexicon.NULLABLE, DdlConstants.NULL);
        }

        { // column e2
            assertThat(tableNode.childrenWithName("e2").size(), is(1));
            final AstNode e2 = tableNode.childrenWithName("e2").get(0);
            assertProperty(e2, StandardDdlLexicon.DATATYPE_NAME, TeiidDataType.VARCHAR.toDdl());
            assertProperty(e2, TeiidDdlLexicon.CreateTable.AUTO_INCREMENT, false);
            assertProperty(e2, StandardDdlLexicon.NULLABLE, "NULL");
        }

        { // column e3
            assertThat(tableNode.childrenWithName("e3").size(), is(1));
            final AstNode e3 = tableNode.childrenWithName("e3").get(0);
            assertProperty(e3, StandardDdlLexicon.DATATYPE_NAME, TeiidDataType.DATE.toDdl());
            assertProperty(e3, TeiidDdlLexicon.CreateTable.AUTO_INCREMENT, false);
            assertProperty(e3, StandardDdlLexicon.NULLABLE, "NULL");
        }

        // constraints
        assertThat(tableNode.getChildren(TeiidDdlLexicon.Constraint.TABLE_ELEMENT).size(), is(3));
        assertThat(tableNode.childrenWithName(CreateTableParser.ACCESS_PATTERN_PREFIX + "1").size(), is(1));
        assertThat(tableNode.childrenWithName(CreateTableParser.ACCESS_PATTERN_PREFIX + "2").size(), is(1));
        assertThat(tableNode.childrenWithName(CreateTableParser.UNIQUE_CONSTRAINT_PREFIX + "1").size(), is(1));
        final Object temp = tableNode.childrenWithName(CreateTableParser.UNIQUE_CONSTRAINT_PREFIX + "1").get(0).getProperty(TeiidDdlLexicon.Constraint.REFERENCES);
        assertThat(temp, is(instanceOf(List.class)));

        @SuppressWarnings( "unchecked" )
        final List<AstNode> references = (List<AstNode>)temp;
        assertThat(references.size(), is(1));
        assertThat(references.get(0), is(tableNode.childrenWithName("e1").get(0)));

        assertThat(tableNode.getChildren(TeiidDdlLexicon.Constraint.INDEX_CONSTRAINT).size(), is(0));
    }

    @Test( expected = ParsingException.class )
    public void shouldFailParsingUnresolvedColumnInPrimaryKey() {
        final String content = "CREATE FOREIGN TABLE G1( e1 integer, e2 varchar, PRIMARY KEY (missingColumn))";
        this.parser.parse(getTokens(content), this.rootNode);
    }

    @Test
    public void shouldParseUnresolvedTableInForeignKey() {
        final String content = "CREATE FOREIGN TABLE G1 (e1 integer, e2 varchar, CONSTRAINT fk_1 FOREIGN KEY (e1, e2) REFERENCES missingTable)";
        final AstNode tableNode = this.parser.parse(getTokens(content), this.rootNode);
        assertThat(tableNode.getName(), is("G1"));
        assertMixinType(tableNode, TeiidDdlLexicon.CreateTable.TABLE_STATEMENT);
        assertProperty(tableNode, TeiidDdlLexicon.SchemaElement.TYPE, SchemaElementType.FOREIGN.toDdl());
        assertThat(tableNode.childrenWithName("fk_1").size(), is(1)); // make sure foreign key still created
    }

    @Test( expected = ParsingException.class )
    public void shouldFailParsingUnresolvedColumnReferenceInForeignKey() {
        this.parser.parse(getTokens("CREATE FOREIGN TABLE refTable( t1 integer, t2 varchar)"), this.rootNode);
        final String content = "CREATE FOREIGN TABLE G1 (e1 integer, e2 varchar, CONSTRAINT fk_1 FOREIGN KEY (e1, e2) REFERENCES refTable (t1, missingColumn))";
        this.parser.parse(getTokens(content), this.rootNode);
    }

    /**
     * See Teiid TestDDLParser#testWrongPrimarykey()
     */
    @Test
    public void shouldParseViewWithoutColumns() {
        final String content = "CREATE VIEW V1 AS SELECT * FROM PM1.G1";
        final AstNode viewNode = this.parser.parse(getTokens(content), this.rootNode);
        assertThat(viewNode.getName(), is("V1"));
        assertMixinType(viewNode, TeiidDdlLexicon.CreateTable.VIEW_STATEMENT);
        assertProperty(viewNode, TeiidDdlLexicon.SchemaElement.TYPE, SchemaElementType.VIRTUAL.toDdl());
        assertProperty(viewNode, TeiidDdlLexicon.CreateTable.QUERY_EXPRESSION, "SELECT * FROM PM1.G1");

        // columns
        assertThat(viewNode.getChildren(TeiidDdlLexicon.CreateTable.TABLE_ELEMENT).size(), is(0));

        // constraints
        assertThat(viewNode.getChildren(TeiidDdlLexicon.Constraint.TABLE_ELEMENT).size(), is(0));
        assertThat(viewNode.getChildren(TeiidDdlLexicon.Constraint.INDEX_CONSTRAINT).size(), is(0));
    }

    /**
     * See Teiid TestDDLParser#testView()
     */
    @Test
    public void shouldParseView() {
        final String content = "CREATE View G1( e1 integer, e2 varchar) OPTIONS (CARDINALITY 12) AS select e1, e2 from foo.bar";
        final AstNode viewNode = this.parser.parse(getTokens(content), this.rootNode);
        assertThat(viewNode.getName(), is("G1"));
        assertMixinType(viewNode, TeiidDdlLexicon.CreateTable.VIEW_STATEMENT);
        assertProperty(viewNode, TeiidDdlLexicon.SchemaElement.TYPE, SchemaElementType.VIRTUAL.toDdl());
        assertProperty(viewNode, TeiidDdlLexicon.CreateTable.QUERY_EXPRESSION, "select e1, e2 from foo.bar");

        // columns
        final List<AstNode> columns = viewNode.getChildren(TeiidDdlLexicon.CreateTable.TABLE_ELEMENT);
        assertThat(columns.size(), is(2));

        if ("e1".equals(columns.get(0).getName())) {
            assertProperty(columns.get(0), StandardDdlLexicon.DATATYPE_NAME, TeiidDataType.INTEGER.toDdl());
            assertThat("e2", is(columns.get(1).getName()));
            assertProperty(columns.get(1), StandardDdlLexicon.DATATYPE_NAME, TeiidDataType.VARCHAR.toDdl());
        } else if ("e1".equals(columns.get(1).getName())) {
            assertProperty(columns.get(1), StandardDdlLexicon.DATATYPE_NAME, TeiidDataType.INTEGER.toDdl());
            assertThat("e2", is(columns.get(0).getName()));
            assertProperty(columns.get(0), StandardDdlLexicon.DATATYPE_NAME, TeiidDataType.VARCHAR.toDdl());
        } else {
            fail("column 'e1' not found");
        }

        // options
        assertThat(viewNode.childrenWithName("CARDINALITY").size(), is(1));
        assertProperty(viewNode.childrenWithName("CARDINALITY").get(0), StandardDdlLexicon.VALUE, "12");

        // constraints
        assertThat(viewNode.getChildren(TeiidDdlLexicon.Constraint.TABLE_ELEMENT).size(), is(0));
        assertThat(viewNode.getChildren(TeiidDdlLexicon.Constraint.INDEX_CONSTRAINT).size(), is(0));
    }

    @Test
    public void shouldParseCreateForeignTableWithoutBody() {
        final String content = "CREATE FOREIGN TABLE FOO";
        final AstNode tableNode = this.parser.parse(getTokens(content), this.rootNode);
        assertThat(tableNode.getName(), is("FOO"));
        assertMixinType(tableNode, TeiidDdlLexicon.CreateTable.TABLE_STATEMENT);
        assertProperty(tableNode, TeiidDdlLexicon.SchemaElement.TYPE, SchemaElementType.FOREIGN.toDdl());

        // columns
        assertThat(tableNode.getChildren(TeiidDdlLexicon.CreateTable.TABLE_ELEMENT).size(), is(0));

        // constraints
        assertThat(tableNode.getChildren(TeiidDdlLexicon.Constraint.TABLE_ELEMENT).size(), is(0));
        assertThat(tableNode.getChildren(TeiidDdlLexicon.Constraint.INDEX_CONSTRAINT).size(), is(0));
    }

    @Test
    public void shouldParseCreateViewWithoutBody() {
        final String content = "CREATE VIEW FOO";
        final AstNode viewNode = this.parser.parse(getTokens(content), this.rootNode);
        assertThat(viewNode.getName(), is("FOO"));
        assertMixinType(viewNode, TeiidDdlLexicon.CreateTable.VIEW_STATEMENT);
        assertProperty(viewNode, TeiidDdlLexicon.SchemaElement.TYPE, SchemaElementType.VIRTUAL.toDdl());

        // columns
        assertThat(viewNode.getChildren(TeiidDdlLexicon.CreateTable.TABLE_ELEMENT).size(), is(0));

        // constraints
        assertThat(viewNode.getChildren(TeiidDdlLexicon.Constraint.TABLE_ELEMENT).size(), is(0));
        assertThat(viewNode.getChildren(TeiidDdlLexicon.Constraint.INDEX_CONSTRAINT).size(), is(0));
    }

    @Test
    public void shouldParseCreateVirtualViewWithoutBody() {
        final String content = "CREATE VIRTUAL VIEW FOO";
        final AstNode viewNode = this.parser.parse(getTokens(content), this.rootNode);
        assertThat(viewNode.getName(), is("FOO"));
        assertMixinType(viewNode, TeiidDdlLexicon.CreateTable.VIEW_STATEMENT);
        assertProperty(viewNode, TeiidDdlLexicon.SchemaElement.TYPE, SchemaElementType.VIRTUAL.toDdl());

        // columns
        assertThat(viewNode.getChildren(TeiidDdlLexicon.CreateTable.TABLE_ELEMENT).size(), is(0));

        // constraints
        assertThat(viewNode.getChildren(TeiidDdlLexicon.Constraint.TABLE_ELEMENT).size(), is(0));
        assertThat(viewNode.getChildren(TeiidDdlLexicon.Constraint.INDEX_CONSTRAINT).size(), is(0));
    }

    // ********* table element tests ***********

    @Test
    public void shouldParseTableElement() {
        final String content = "symbol string";
        final AstNode node = this.parser.parseTableElement(getTokens(content), this.rootNode);

        assertMixinType(node, TeiidDdlLexicon.CreateTable.TABLE_ELEMENT);
        assertThat(node.getName(), is("symbol"));
        assertProperty(node, StandardDdlLexicon.DATATYPE_NAME, TeiidDdlConstants.TeiidDataType.STRING.toDdl());
        assertThat(this.rootNode.getChildCount(), is(1));
    }

    @Test
    public void shouldParseTableElementWithDataTypeLength() {
        final String content = "company_name varchar(256)";
        final AstNode node = this.parser.parseTableElement(getTokens(content), this.rootNode);

        assertMixinType(node, TeiidDdlLexicon.CreateTable.TABLE_ELEMENT);
        assertThat(node.getName(), is("company_name"));
        assertProperty(node, StandardDdlLexicon.DATATYPE_NAME, TeiidDdlConstants.TeiidDataType.VARCHAR.toDdl());
        assertProperty(node, StandardDdlLexicon.DATATYPE_LENGTH, 256L);
    }

    @Test
    public void shouldParseTableElementWithNotNull() {
        final String content = "ACCOUNT_ID long NOT NULL";
        final AstNode node = this.parser.parseTableElement(getTokens(content), this.rootNode);

        assertMixinType(node, TeiidDdlLexicon.CreateTable.TABLE_ELEMENT);
        assertThat(node.getName(), is("ACCOUNT_ID"));
        assertProperty(node, StandardDdlLexicon.DATATYPE_NAME, TeiidDdlConstants.TeiidDataType.LONG.toDdl());
        assertProperty(node, StandardDdlLexicon.NULLABLE, "NOT NULL");
    }

    @Test
    public void shouldParseTableElementWithQuotedDefaultValue() {
        final String content = "DATEOPENED timestamp DEFAULT 'CURRENT_TIMESTAMP'";
        final AstNode node = this.parser.parseTableElement(getTokens(content), this.rootNode);

        assertMixinType(node, TeiidDdlLexicon.CreateTable.TABLE_ELEMENT);
        assertThat(node.getName(), is("DATEOPENED"));
        assertProperty(node, StandardDdlLexicon.DATATYPE_NAME, TeiidDdlConstants.TeiidDataType.TIMESTAMP.toDdl());
        assertProperty(node, StandardDdlLexicon.DEFAULT_OPTION, StandardDdlLexicon.DEFAULT_ID_DATETIME);
        assertProperty(node, StandardDdlLexicon.DEFAULT_VALUE, "CURRENT_TIMESTAMP");
    }

    @Test
    public void shouldParseTableElementWithOptionsClause() {
        final String content = "DATECLOSED timestamp OPTIONS (ANNOTATION 'This is the date closed', NAMEINSOURCE '`DATECLOSED`', NATIVE_TYPE 'TIMESTAMP')";
        final AstNode node = this.parser.parseTableElement(getTokens(content), this.rootNode);

        assertMixinType(node, TeiidDdlLexicon.CreateTable.TABLE_ELEMENT);
        assertThat(node.getName(), is("DATECLOSED"));
        assertProperty(node, StandardDdlLexicon.DATATYPE_NAME, TeiidDdlConstants.TeiidDataType.TIMESTAMP.toDdl());

        // make sure the is an options clause
        final List<AstNode> kids = node.getChildren();
        assertThat(kids.size(), is(3));

        for (final AstNode kid : kids) {
            assertMixinType(kid, StandardDdlLexicon.TYPE_STATEMENT_OPTION);
        }
    }

    @Test
    public void shouldParseTableElementWithPrimaryKeyConstraint() {
        final String content = "KEY STRING(9) PRIMARY KEY";
        final AstNode columnNode = this.parser.parseTableElement(getTokens(content), this.rootNode);

        assertMixinType(columnNode, TeiidDdlLexicon.CreateTable.TABLE_ELEMENT);
        assertThat(columnNode.getName(), is("KEY"));
        assertProperty(columnNode, StandardDdlLexicon.DATATYPE_NAME, TeiidDdlConstants.TeiidDataType.STRING.toDdl());
        assertProperty(columnNode, StandardDdlLexicon.DATATYPE_LENGTH, 9L);

        // check constraint
        final List<AstNode> kids = this.rootNode.getChildren();
        assertThat(kids.size(), is(2)); // one for column, one for constraint

        final AstNode constraintNode = ((kids.get(0) == columnNode) ? kids.get(1) : kids.get(0));
        assertThat(constraintNode.getMixins(), hasItem(TeiidDdlLexicon.Constraint.TABLE_ELEMENT));
        assertProperty(constraintNode, TeiidDdlLexicon.Constraint.TYPE, DdlConstants.PRIMARY_KEY);

        final Object temp = constraintNode.getProperty(TeiidDdlLexicon.Constraint.REFERENCES);
        assertThat(temp, is(instanceOf(List.class)));

        @SuppressWarnings( "unchecked" )
        final List<AstNode> references = (List<AstNode>)temp;
        assertThat(references.size(), is(1));
        assertThat(references.get(0), is(columnNode));
    }

    @Test
    public void shouldParseTableElementWithPrimaryUniqueConstraint() {
        final String content = "KEY VARCHAR(9) UNIQUE";
        final AstNode columnNode = this.parser.parseTableElement(getTokens(content), this.rootNode);

        assertMixinType(columnNode, TeiidDdlLexicon.CreateTable.TABLE_ELEMENT);
        assertThat(columnNode.getName(), is("KEY"));
        assertProperty(columnNode, StandardDdlLexicon.DATATYPE_NAME, TeiidDdlConstants.TeiidDataType.VARCHAR.toDdl());
        assertProperty(columnNode, StandardDdlLexicon.DATATYPE_LENGTH, 9L);

        // check constraint
        final List<AstNode> kids = this.rootNode.getChildren();
        assertThat(kids.size(), is(2)); // one for column, one for constraint

        final AstNode constraintNode = ((kids.get(0) == columnNode) ? kids.get(1) : kids.get(0));
        assertThat(constraintNode.getMixins(), hasItem(TeiidDdlLexicon.Constraint.TABLE_ELEMENT));
        assertProperty(constraintNode, TeiidDdlLexicon.Constraint.TYPE, DdlConstants.UNIQUE);

        final Object temp = constraintNode.getProperty(TeiidDdlLexicon.Constraint.REFERENCES);
        assertThat(temp, is(instanceOf(List.class)));

        @SuppressWarnings( "unchecked" )
        final List<AstNode> references = (List<AstNode>)temp;
        assertThat(references.size(), is(1));
        assertThat(references.get(0), is(columnNode));
    }

    @Test
    public void shouldParseTableElementWithIndexConstraint() {
        final String content = "KEY STRING(9) INDEX";
        final AstNode columnNode = this.parser.parseTableElement(getTokens(content), this.rootNode);

        assertMixinType(columnNode, TeiidDdlLexicon.CreateTable.TABLE_ELEMENT);
        assertThat(columnNode.getName(), is("KEY"));
        assertProperty(columnNode, StandardDdlLexicon.DATATYPE_NAME, TeiidDdlConstants.TeiidDataType.STRING.toDdl());

        // check constraint
        final List<AstNode> kids = this.rootNode.getChildren();
        assertThat(kids.size(), is(2)); // one for column, one for constraint

        final AstNode constraintNode = ((kids.get(0) == columnNode) ? kids.get(1) : kids.get(0));
        assertThat(constraintNode.getMixins(), hasItem(TeiidDdlLexicon.Constraint.INDEX_CONSTRAINT));
        assertProperty(constraintNode, TeiidDdlLexicon.Constraint.TYPE, DdlConstants.INDEX);
        assertThat(constraintNode.getProperty(TeiidDdlLexicon.Constraint.EXPRESSION), is(nullValue()));
    }

    @Test
    public void shouldParseTableElementWithAutoIncrementIndexConstraint() {
        final String content = "e5 integer auto_increment INDEX";
        final AstNode columnNode = this.parser.parseTableElement(getTokens(content), this.rootNode);

        assertMixinType(columnNode, TeiidDdlLexicon.CreateTable.TABLE_ELEMENT);
        assertThat(columnNode.getName(), is("e5"));
        assertProperty(columnNode, StandardDdlLexicon.DATATYPE_NAME, TeiidDdlConstants.TeiidDataType.INTEGER.toDdl());
        assertProperty(columnNode, TeiidDdlLexicon.CreateTable.AUTO_INCREMENT, true);
        assertProperty(columnNode, StandardDdlLexicon.NULLABLE, DdlConstants.NULL);

        // check constraint
        final List<AstNode> kids = this.rootNode.getChildren();
        assertThat(kids.size(), is(2)); // one for column, one for constraint

        final AstNode constraintNode = ((kids.get(0) == columnNode) ? kids.get(1) : kids.get(0));
        assertThat(constraintNode.getMixins(), hasItem(TeiidDdlLexicon.Constraint.INDEX_CONSTRAINT));
        assertProperty(constraintNode, TeiidDdlLexicon.Constraint.TYPE, DdlConstants.INDEX);
        assertThat(constraintNode.getProperty(TeiidDdlLexicon.Constraint.EXPRESSION), is(nullValue()));
    }

    @Test
    public void shouldParseTableElementWithAllClauses() {
        final String name = "foo";
        final int precision = 9;
        final int scale = 3;
        final String value = "bar";
        final String content = name
                               + " bigdecimal("
                               + precision
                               + ','
                               + scale
                               + ") NOT NULL auto_increment index DEFAULT '"
                               + value
                               + "' OPTIONS (CARDINALITY 12, UUID 'uuid2',  UPDATABLE 'true', FOO 'BAR', ANNOTATION 'Test Table')";
        final AstNode columnNode = this.parser.parseTableElement(getTokens(content), this.rootNode);

        assertThat(columnNode.getName(), is(name));
        assertProperty(columnNode, StandardDdlLexicon.DATATYPE_NAME, TeiidDdlConstants.TeiidDataType.BIGDECIMAL.toDdl());
        assertProperty(columnNode, StandardDdlLexicon.DATATYPE_PRECISION, precision);
        assertProperty(columnNode, StandardDdlLexicon.DATATYPE_SCALE, scale);
        assertProperty(columnNode, StandardDdlLexicon.DEFAULT_OPTION, StandardDdlLexicon.DEFAULT_ID_LITERAL);
        assertProperty(columnNode, StandardDdlLexicon.DEFAULT_VALUE, value);
        assertProperty(columnNode, TeiidDdlLexicon.CreateTable.AUTO_INCREMENT, true);
        assertProperty(columnNode, StandardDdlLexicon.NULLABLE, "NOT NULL");

        // make sure root has 2 children (column, constraint)
        final List<AstNode> rootKids = this.rootNode.getChildren();
        assertThat(rootKids.size(), is(2));

        // check constraint
        final AstNode constraintNode = ((rootKids.get(0) == columnNode) ? rootKids.get(1) : rootKids.get(0));
        assertThat(constraintNode.getMixins(), hasItem(TeiidDdlLexicon.Constraint.INDEX_CONSTRAINT));
        assertProperty(constraintNode, TeiidDdlLexicon.Constraint.TYPE, DdlConstants.INDEX);
        assertThat(constraintNode.getProperty(TeiidDdlLexicon.Constraint.EXPRESSION), is(nullValue()));

        // make sure there are 5 options clauses on the column
        final List<AstNode> kids = columnNode.getChildren();
        assertThat(kids.size(), is(5));

        for (final AstNode kid : kids) {
            assertMixinType(kid, StandardDdlLexicon.TYPE_STATEMENT_OPTION);
        }
    }

    // ********* table body constraint tests ***********

    @Test
    public void shouldParseNamedIndexConstraint() {
        final String content = "CONSTRAINT fbi INDEX (UPPER(e2))";
        assertThat(this.parser.parseTableBodyConstraint(getTokens(content), this.rootNode), is(true));

        // check constraint
        final List<AstNode> kids = this.rootNode.getChildren();
        assertThat(kids.size(), is(1));

        final AstNode constraintNode = kids.get(0);
        assertThat(constraintNode.getMixins(), hasItem(TeiidDdlLexicon.Constraint.INDEX_CONSTRAINT));
        assertThat(constraintNode.getName(), is("fbi"));
        assertProperty(constraintNode, TeiidDdlLexicon.Constraint.TYPE, DdlConstants.INDEX);
        assertProperty(constraintNode, TeiidDdlLexicon.Constraint.EXPRESSION, "UPPER(e2)");
    }

    @Test
    public void shouldParseUnnamedIndexConstraint() {
        final String content = " INDEX (UPPER(e2))";
        assertThat(this.parser.parseTableBodyConstraint(getTokens(content), this.rootNode), is(true));

        // check constraint
        final List<AstNode> kids = this.rootNode.getChildren();
        assertThat(kids.size(), is(1));

        final AstNode constraintNode = kids.get(0);
        assertThat(constraintNode.getMixins(), hasItem(TeiidDdlLexicon.Constraint.INDEX_CONSTRAINT));
        assertProperty(constraintNode, TeiidDdlLexicon.Constraint.TYPE, DdlConstants.INDEX);
        assertThat(constraintNode.getName(), is(CreateTableParser.INDEX_PREFIX + "1"));
        assertProperty(constraintNode, TeiidDdlLexicon.Constraint.EXPRESSION, "UPPER(e2)");
    }

    @Test
    public void shouldParseNamedPrimaryKeyConstraint() {
        final AstNode col1Node = new AstNode(this.rootNode, "col1");
        final AstNode col2Node = new AstNode(this.rootNode, "col2");

        final String content = "CONSTRAINT pk_1 PRIMARY KEY (col1, col2)";
        assertThat(this.parser.parseTableBodyConstraint(getTokens(content), this.rootNode), is(true));
        assertThat(this.rootNode.getChildCount(), is(3)); // 2 columns, 1 constraint

        final List<AstNode> kids = this.rootNode.childrenWithName("pk_1");
        assertThat(kids.size(), is(1)); // constraint node

        final AstNode constraintNode = kids.get(0);
        assertThat(constraintNode.getMixins(), hasItem(TeiidDdlLexicon.Constraint.TABLE_ELEMENT));
        assertThat(constraintNode.getName(), is("pk_1"));
        assertProperty(constraintNode, TeiidDdlLexicon.Constraint.TYPE, DdlConstants.PRIMARY_KEY);

        // referenced columns
        @SuppressWarnings( "unchecked" )
        final List<AstNode> refs = (List<AstNode>)constraintNode.getProperty(TeiidDdlLexicon.Constraint.REFERENCES);
        assertThat(refs.size(), is(2));
        assertThat(refs, hasItems(col1Node, col2Node));
    }

    @Test
    public void shouldParseUnnamedPrimaryKeyConstraint() {
        final AstNode col1Node = new AstNode(this.rootNode, "col1");
        final AstNode col2Node = new AstNode(this.rootNode, "col2");

        final String content = "PRIMARY KEY (col1, col2)";
        assertThat(this.parser.parseTableBodyConstraint(getTokens(content), this.rootNode), is(true));
        assertThat(this.rootNode.getChildCount(), is(3)); // 2 columns, 1 constraint

        final List<AstNode> kids = this.rootNode.childrenWithName(CreateTableParser.PRIMARY_KEY_PREFIX + "1");
        assertThat(kids.size(), is(1)); // constraint node

        final AstNode constraintNode = kids.get(0);
        assertThat(constraintNode.getMixins(), hasItem(TeiidDdlLexicon.Constraint.TABLE_ELEMENT));
        assertProperty(constraintNode, TeiidDdlLexicon.Constraint.TYPE, DdlConstants.PRIMARY_KEY);

        // referenced columns
        @SuppressWarnings( "unchecked" )
        final List<AstNode> refs = (List<AstNode>)constraintNode.getProperty(TeiidDdlLexicon.Constraint.REFERENCES);
        assertThat(refs.size(), is(2));
        assertThat(refs, hasItems(col1Node, col2Node));
    }

    @Test
    public void shouldParseNamedUniqueConstraint() {
        final AstNode col1Node = new AstNode(this.rootNode, "col1");
        final String content = "CONSTRAINT uk_1 UNIQUE (col1)";
        assertThat(this.parser.parseTableBodyConstraint(getTokens(content), this.rootNode), is(true));
        assertThat(this.rootNode.getChildCount(), is(2)); // 1 column, 1 constraint

        final List<AstNode> kids = this.rootNode.childrenWithName("uk_1");
        assertThat(kids.size(), is(1)); // constraint node

        final AstNode constraintNode = kids.get(0);
        assertThat(constraintNode.getMixins(), hasItem(TeiidDdlLexicon.Constraint.TABLE_ELEMENT));
        assertProperty(constraintNode, TeiidDdlLexicon.Constraint.TYPE, TeiidReservedWord.UNIQUE.toDdl());

        // referenced columns
        @SuppressWarnings( "unchecked" )
        final List<AstNode> refs = (List<AstNode>)constraintNode.getProperty(TeiidDdlLexicon.Constraint.REFERENCES);
        assertThat(refs.size(), is(1));
        assertThat(refs, hasItem(col1Node));
    }

    @Test
    public void shouldParseUnnamedUniqueConstraint() {
        final AstNode col1Node = new AstNode(this.rootNode, "col1");
        final String content = "UNIQUE (col1)";
        assertThat(this.parser.parseTableBodyConstraint(getTokens(content), this.rootNode), is(true));
        assertThat(this.rootNode.getChildCount(), is(2)); // 1 column, 1 constraint

        final List<AstNode> kids = this.rootNode.childrenWithName(CreateTableParser.UNIQUE_CONSTRAINT_PREFIX + "1");
        assertThat(kids.size(), is(1)); // constraint node

        final AstNode constraintNode = kids.get(0);
        assertThat(constraintNode.getMixins(), hasItem(TeiidDdlLexicon.Constraint.TABLE_ELEMENT));
        assertProperty(constraintNode, TeiidDdlLexicon.Constraint.TYPE, TeiidReservedWord.UNIQUE.toDdl());

        // referenced columns
        @SuppressWarnings( "unchecked" )
        final List<AstNode> refs = (List<AstNode>)constraintNode.getProperty(TeiidDdlLexicon.Constraint.REFERENCES);
        assertThat(refs.size(), is(1));
        assertThat(refs, hasItem(col1Node));
    }

    @Test
    public void shouldParseNamedAccessPatternConstraint() {
        final AstNode col1Node = new AstNode(this.rootNode, "col1");
        final AstNode col2Node = new AstNode(this.rootNode, "col2");
        final String content = "CONSTRAINT ap_1 ACCESSPATTERN (col1, col2)";
        assertThat(this.parser.parseTableBodyConstraint(getTokens(content), this.rootNode), is(true));
        assertThat(this.rootNode.getChildCount(), is(3)); // 2 columns, 1 constraint

        final List<AstNode> kids = this.rootNode.childrenWithName("ap_1");
        assertThat(kids.size(), is(1)); // constraint node

        final AstNode constraintNode = kids.get(0);
        assertThat(constraintNode.getMixins(), hasItem(TeiidDdlLexicon.Constraint.TABLE_ELEMENT));
        assertProperty(constraintNode, TeiidDdlLexicon.Constraint.TYPE, TeiidNonReservedWord.ACCESSPATTERN.toDdl());

        // referenced columns
        @SuppressWarnings( "unchecked" )
        final List<AstNode> refs = (List<AstNode>)constraintNode.getProperty(TeiidDdlLexicon.Constraint.REFERENCES);
        assertThat(refs.size(), is(2));
        assertThat(refs, hasItems(col1Node, col2Node));
    }

    @Test
    public void shouldParseUnnamedAccessPatternConstraint() {
        final AstNode col1Node = new AstNode(this.rootNode, "col1");
        final AstNode col2Node = new AstNode(this.rootNode, "col2");
        final String content = "ACCESSPATTERN (col1, col2)";
        assertThat(this.parser.parseTableBodyConstraint(getTokens(content), this.rootNode), is(true));
        assertThat(this.rootNode.getChildCount(), is(3)); // 2 columns, 1 constraint

        final List<AstNode> kids = this.rootNode.childrenWithName(CreateTableParser.ACCESS_PATTERN_PREFIX + "1");
        assertThat(kids.size(), is(1)); // constraint node

        final AstNode constraintNode = kids.get(0);
        assertThat(constraintNode.getMixins(), hasItem(TeiidDdlLexicon.Constraint.TABLE_ELEMENT));
        assertProperty(constraintNode, TeiidDdlLexicon.Constraint.TYPE, TeiidNonReservedWord.ACCESSPATTERN.toDdl());

        // referenced columns
        @SuppressWarnings( "unchecked" )
        final List<AstNode> refs = (List<AstNode>)constraintNode.getProperty(TeiidDdlLexicon.Constraint.REFERENCES);
        assertThat(refs.size(), is(2));
        assertThat(refs, hasItems(col1Node, col2Node));
    }

    @Test
    public void shouldParseNamedForeignKeyConstraintWithReferencesRefs() {
        final AstNode tableNode = new AstNode(this.rootNode, "tableWithForeignKey");

        final String col1 = "col1";
        final String col2 = "col2";
        final AstNode col1Node = new AstNode(tableNode, col1);
        final AstNode col2Node = new AstNode(tableNode, col2);

        final String refTable = "refTable";
        final AstNode refTableNode = new AstNode(this.rootNode, refTable);
        refTableNode.setProperty(JcrConstants.JCR_PRIMARY_TYPE, TeiidDdlLexicon.CreateTable.TABLE_STATEMENT);

        final String refCol1 = "refCol1";
        final String refCol2 = "refCol2";
        final AstNode refCol1Node = new AstNode(refTableNode, refCol1);
        final AstNode refCol2Node = new AstNode(refTableNode, refCol2);

        final String name = "fk_1";
        final String content = "CONSTRAINT " + name + " FOREIGN KEY (" + col1 + ", " + col2 + ") REFERENCES " + refTable + '('
                               + refCol1 + ',' + refCol2 + ')';
        assertThat(this.parser.parseTableBodyConstraint(getTokens(content), tableNode), is(true));
        assertThat(tableNode.getChildCount(), is(3)); // 2 columns, 1 constraint

        final List<AstNode> kids = tableNode.childrenWithName(name);
        assertThat(kids.size(), is(1)); // constraint node

        final AstNode constraintNode = kids.get(0);
        assertThat(constraintNode.getMixins(), hasItem(TeiidDdlLexicon.Constraint.FOREIGN_KEY_CONSTRAINT));
        assertThat(constraintNode.getName(), is(name));
        assertProperty(constraintNode, TeiidDdlLexicon.Constraint.TYPE, DdlConstants.FOREIGN_KEY);
        assertProperty(constraintNode, TeiidDdlLexicon.Constraint.TABLE_REFERENCE, refTableNode);

        { // referenced columns
            @SuppressWarnings( "unchecked" )
            final List<AstNode> refs = (List<AstNode>)constraintNode.getProperty(TeiidDdlLexicon.Constraint.REFERENCES);
            assertThat(refs.size(), is(2));
            assertThat(refs, hasItems(col1Node, col2Node));
        }

        { // references referenced columns
            @SuppressWarnings( "unchecked" )
            final List<AstNode> refRefs = (List<AstNode>)constraintNode.getProperty(TeiidDdlLexicon.Constraint.TABLE_REFERENCE_REFERENCES);
            assertThat(refRefs.size(), is(2));
            assertThat(refRefs, hasItems(refCol1Node, refCol2Node));
        }
    }

    @Test
    public void shouldParseNamedForeignKeyConstraintNoReferencesRefs() {
        final AstNode tableNode = new AstNode(this.rootNode, "tableWithForeignKey");

        final String col1 = "col1";
        final String col2 = "col2";
        final AstNode col1Node = new AstNode(tableNode, col1);
        final AstNode col2Node = new AstNode(tableNode, col2);

        final String refTable = "refTable";
        final AstNode refTableNode = new AstNode(this.rootNode, refTable);
        refTableNode.setProperty(JcrConstants.JCR_PRIMARY_TYPE, TeiidDdlLexicon.CreateTable.TABLE_STATEMENT);

        final String name = "fk_1";
        final String content = "CONSTRAINT " + name + " FOREIGN KEY (" + col1 + ", " + col2 + ") REFERENCES " + refTable;
        assertThat(this.parser.parseTableBodyConstraint(getTokens(content), tableNode), is(true));
        assertThat(tableNode.getChildCount(), is(3)); // 2 columns, 1 constraint

        final List<AstNode> kids = tableNode.childrenWithName(name);
        assertThat(kids.size(), is(1)); // constraint node

        final AstNode constraintNode = kids.get(0);
        assertThat(constraintNode.getMixins(), hasItem(TeiidDdlLexicon.Constraint.FOREIGN_KEY_CONSTRAINT));
        assertThat(constraintNode.getName(), is(name));
        assertProperty(constraintNode, TeiidDdlLexicon.Constraint.TYPE, DdlConstants.FOREIGN_KEY);
        assertProperty(constraintNode, TeiidDdlLexicon.Constraint.TABLE_REFERENCE, refTableNode);

        { // referenced columns
            @SuppressWarnings( "unchecked" )
            final List<AstNode> refs = (List<AstNode>)constraintNode.getProperty(TeiidDdlLexicon.Constraint.REFERENCES);
            assertThat(refs.size(), is(2));
            assertThat(refs, hasItems(col1Node, col2Node));
        }

        { // references referenced columns
            @SuppressWarnings( "unchecked" )
            final List<AstNode> refRefs = (List<AstNode>)constraintNode.getProperty(TeiidDdlLexicon.Constraint.TABLE_REFERENCE_REFERENCES);
            assertThat(refRefs, is(nullValue()));
        }
    }

    // ********* parse query expression tests ***********

    @Test
    public void shouldParseQueryExpression() {
        final String content = "WITH X (Y, Z) AS (SELECT 1, 2)";
        assertThat(this.parser.parseQueryExpression(getTokens(content), this.rootNode), is(true));
        assertProperty(this.rootNode, TeiidDdlLexicon.CreateTable.QUERY_EXPRESSION, content);
    }

    @Test
    public void shouldNotParseEmptyQueryExpression() {
        assertThat(this.parser.parseQueryExpression(getTokens(""), this.rootNode), is(false));
    }

    // ********* parse identifier list tests ***********

    @Test( expected = ParsingException.class )
    public void shouldNotParseEmptyIdentifierList() {
        final String content = "()";
        this.parser.parseIdentifierList(getTokens(content));
    }

    @Test
    public void shouldParseIdentifierListWithOneElement() {
        final String id = "a";
        final String content = '(' + id + ')';
        final List<String> idList = this.parser.parseIdentifierList(getTokens(content));

        assertThat(idList.size(), is(1));
        assertThat(idList.get(0), is(id));
    }

    @Test
    public void shouldParseIdentifierListWithMultipleElements() {
        final String a = "a";
        final String b = "b";
        final String c = "c";
        final String content = '(' + a + ',' + b + ',' + c + ')';
        final List<String> idList = this.parser.parseIdentifierList(getTokens(content));

        assertThat(idList.size(), is(3));
        assertThat(idList.get(0), is(a));
        assertThat(idList.get(1), is(b));
        assertThat(idList.get(2), is(c));
    }

    @Test
    public void shouldParseExpressionLists() {
        assertThat(CreateTableParser.contains("UPPER(e2)", "e2"), is(true));
        assertThat(CreateTableParser.contains("UPPER(e2)", "E2"), is(true));
        assertThat(CreateTableParser.contains("UPPER(e1, e2)", "e3"), is(false));

        assertThat(CreateTableParser.contains("abc, def", "def"), is(true));
        assertThat(CreateTableParser.contains("abc, def", "DEF"), is(true));
    }

    //
    // @Test
    // public void shouldTestWhitespace() {
    // final String prevValue = "a";
    // final int currCol = 3;
    // final int prevCol = 5;
    // final int currLine = 5;
    // final int prevLine = 4;
    //
    // final Position curr = new Position(currCol-1, currLine, currCol);
    // final Position prev = new Position(prevCol-1, prevLine, prevCol);
    // assertThat(getParser().getWhitespace(curr, prev, prevValue), is("\n   "));
    // }

    // ********* parse temporary table tests ***********

    @Test( expected = ParsingException.class )
    public void shouldFailParsingLocalTemporaryTableWithUnknownPrimaryKeyColumn() {
        final String content = "Create local TEMPORARY table x(c1 boolean, c2 byte, primary key (missingColumn))";
        this.parser.parse(getTokens(content), this.rootNode);
    }

    @Test( expected = ParsingException.class )
    public void shouldFailParsingForeignTemporaryTableWithoutOnClause() {
        final String content = "create foreign temporary table x (id string)";
        this.parser.parse(getTokens(content), this.rootNode);
    }

    @Test
    public void shouldParseForeignTemporaryTable() {
        final String content = "create foreign temporary table x (e1 string options (nameinsource 'a'), e2 integer, e3 string, primary key (e1)) options (cardinality 1000, updatable true) on pm1";
        final AstNode tableNode = this.parser.parse(getTokens(content), this.rootNode);
        assertThat(tableNode, is(notNullValue()));
        assertThat(tableNode.getName(), is("x"));
        assertMixinType(tableNode, TeiidDdlLexicon.CreateTable.FOREIGN_TEMP_TABLE_STATEMENT);
        assertProperty(tableNode, TeiidDdlLexicon.SchemaElement.TYPE, SchemaElementType.VIRTUAL.toDdl());
        assertProperty(tableNode, TeiidDdlLexicon.CreateTable.INCLUDE_LOCAL_KEYWORD, false);
        assertProperty(tableNode, TeiidDdlLexicon.CreateTable.SCHEMA_REFERENCE, "pm1");

        final List<AstNode> kids = tableNode.getChildren();
        assertThat(kids.size(), is(6)); // 3 columns, primary key, 2 options
    }

    @Test
    public void shouldParseForeignTemporaryTableWithLocalKeyword() {
        final String content = "create local foreign temporary table tempTable (x string, y decimal) options (cardinality 10000) on source";
        final AstNode tableNode = this.parser.parse(getTokens(content), this.rootNode);
        assertThat(tableNode, is(notNullValue()));
        assertThat(tableNode.getName(), is("tempTable"));
        assertMixinType(tableNode, TeiidDdlLexicon.CreateTable.FOREIGN_TEMP_TABLE_STATEMENT);
        assertProperty(tableNode, TeiidDdlLexicon.SchemaElement.TYPE, SchemaElementType.VIRTUAL.toDdl());
        assertProperty(tableNode, TeiidDdlLexicon.CreateTable.INCLUDE_LOCAL_KEYWORD, true);
        assertProperty(tableNode, TeiidDdlLexicon.CreateTable.SCHEMA_REFERENCE, "source");

        final List<AstNode> kids = tableNode.getChildren();
        assertThat(kids.size(), is(3)); // 2 columns, 1 option

        final List<AstNode> temp = tableNode.childrenWithName("x");
        assertThat(temp.size(), is(1));
        assertMixinType(temp.get(0), TeiidDdlLexicon.CreateTable.TABLE_ELEMENT);
    }

    @Test
    public void shouldParseLocalTemporaryTableWithLocalKeyword() {
        final String content = "create local temporary table x (y string)";
        final AstNode tableNode = this.parser.parse(getTokens(content), this.rootNode);
        assertThat(tableNode, is(notNullValue()));
        assertThat(tableNode.getName(), is("x"));
        assertMixinType(tableNode, TeiidDdlLexicon.CreateTable.LOCAL_TEMP_TABLE_STATEMENT);
        assertProperty(tableNode, TeiidDdlLexicon.SchemaElement.TYPE, SchemaElementType.VIRTUAL.toDdl());
        assertProperty(tableNode, TeiidDdlLexicon.CreateTable.INCLUDE_LOCAL_KEYWORD, true);
        assertProperty(tableNode, TeiidDdlLexicon.CreateTable.PRIMARY_KEY_COLUMNS, null);
        assertProperty(tableNode, TeiidDdlLexicon.CreateTable.INCLUDE_ON_COMMIT_CLAUSE, false);

        final List<AstNode> kids = tableNode.getChildren();
        assertThat(kids.size(), is(1));

        final AstNode tempTableElement = kids.get(0);
        assertThat(tempTableElement.getName(), is("y"));
        assertMixinType(tempTableElement, TeiidDdlLexicon.CreateTable.TEMP_TABLE_ELEMENT);
        assertProperty(tempTableElement, StandardDdlLexicon.DATATYPE_NAME, TeiidDataType.STRING.toDdl());
        assertProperty(tempTableElement, TeiidDdlLexicon.CreateTable.INCLUDE_SERIAL_ALIAS, false);
        assertProperty(tempTableElement, StandardDdlLexicon.NULLABLE, "NULL");
    }

    @Test
    public void shouldParseLocalTemporaryTableWithNotNullColumn() {
        final String content = "Create local TEMPORARY table x(c1 boolean not null, c2 byte)";
        final AstNode tableNode = this.parser.parse(getTokens(content), this.rootNode);
        assertThat(tableNode, is(notNullValue()));
        assertThat(tableNode.getName(), is("x"));
        assertMixinType(tableNode, TeiidDdlLexicon.CreateTable.LOCAL_TEMP_TABLE_STATEMENT);
        assertProperty(tableNode, TeiidDdlLexicon.SchemaElement.TYPE, SchemaElementType.VIRTUAL.toDdl());
        assertProperty(tableNode, TeiidDdlLexicon.CreateTable.INCLUDE_LOCAL_KEYWORD, true);
        assertProperty(tableNode, TeiidDdlLexicon.CreateTable.PRIMARY_KEY_COLUMNS, null);
        assertProperty(tableNode, TeiidDdlLexicon.CreateTable.INCLUDE_ON_COMMIT_CLAUSE, false);

        final List<AstNode> kids = tableNode.getChildren();
        assertThat(kids.size(), is(2));

        boolean foundC1 = false;
        boolean foundC2 = false;

        for (final AstNode tempTableElement : kids) {
            assertMixinType(tempTableElement, TeiidDdlLexicon.CreateTable.TEMP_TABLE_ELEMENT);
            assertProperty(tempTableElement, TeiidDdlLexicon.CreateTable.INCLUDE_SERIAL_ALIAS, false);

            if (!foundC1 && tempTableElement.getName().equals("c1")) {
                assertProperty(tempTableElement, StandardDdlLexicon.DATATYPE_NAME, TeiidDataType.BOOLEAN.toDdl());
                assertProperty(tempTableElement, StandardDdlLexicon.NULLABLE, "NOT NULL");
                foundC1 = true;
            } else if (!foundC2 && tempTableElement.getName().equals("c2")) {
                assertProperty(tempTableElement, StandardDdlLexicon.DATATYPE_NAME, TeiidDataType.BYTE.toDdl());
                assertProperty(tempTableElement, StandardDdlLexicon.NULLABLE, "NULL");
                foundC2 = true;
            } else {
                fail();
            }
        }
    }

    @Test
    public void shouldParseLocalTemporaryTableWithOnCommitClause() {
        final String content = "create local temporary table #temp (id integer, name string, bits integer) ON COMMIT PRESERVE ROWS";
        final AstNode tableNode = this.parser.parse(getTokens(content), this.rootNode);
        assertThat(tableNode, is(notNullValue()));
        assertThat(tableNode.getName(), is("#temp"));
        assertMixinType(tableNode, TeiidDdlLexicon.CreateTable.LOCAL_TEMP_TABLE_STATEMENT);
        assertProperty(tableNode, TeiidDdlLexicon.SchemaElement.TYPE, SchemaElementType.VIRTUAL.toDdl());
        assertProperty(tableNode, TeiidDdlLexicon.CreateTable.INCLUDE_LOCAL_KEYWORD, true);
        assertProperty(tableNode, TeiidDdlLexicon.CreateTable.PRIMARY_KEY_COLUMNS, null);
        assertProperty(tableNode, TeiidDdlLexicon.CreateTable.INCLUDE_ON_COMMIT_CLAUSE, true);

        final List<AstNode> kids = tableNode.getChildren();
        assertThat(kids.size(), is(3));
    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void shouldParseLocalTemporaryTableWithPrimaryKey() {
        final String content = "Create local TEMPORARY table x(c1 boolean, c2 byte, primary key (c2))";
        final AstNode tableNode = this.parser.parse(getTokens(content), this.rootNode);
        assertThat(tableNode, is(notNullValue()));
        assertThat(tableNode.getName(), is("x"));
        assertMixinType(tableNode, TeiidDdlLexicon.CreateTable.LOCAL_TEMP_TABLE_STATEMENT);
        assertProperty(tableNode, TeiidDdlLexicon.SchemaElement.TYPE, SchemaElementType.VIRTUAL.toDdl());
        assertProperty(tableNode, TeiidDdlLexicon.CreateTable.INCLUDE_LOCAL_KEYWORD, true);
        assertProperty(tableNode, TeiidDdlLexicon.CreateTable.INCLUDE_ON_COMMIT_CLAUSE, false);

        final List<AstNode> kids = tableNode.getChildren();
        assertThat(kids.size(), is(2)); // 2 columns

        final Object primaryKeyColumns = tableNode.getProperty(TeiidDdlLexicon.CreateTable.PRIMARY_KEY_COLUMNS);
        assertThat(primaryKeyColumns, is(notNullValue()));
        assertThat(primaryKeyColumns, is(instanceOf(List.class)));
        assertThat(((List<AstNode>)primaryKeyColumns).size(), is(1));
        assertThat(((List<AstNode>)primaryKeyColumns).get(0).getName(), is("c2"));
    }

    @Test
    public void shouldParseLocalTemporaryTableWithSerialAlias() {
        final String content = "create local temporary table x (c1 serial, c2 integer, primary key (c1)) ON COMMIT PRESERVE ROWS";
        final AstNode tableNode = this.parser.parse(getTokens(content), this.rootNode);
        assertThat(tableNode, is(notNullValue()));
        assertThat(tableNode.getName(), is("x"));
        assertMixinType(tableNode, TeiidDdlLexicon.CreateTable.LOCAL_TEMP_TABLE_STATEMENT);
        assertProperty(tableNode, TeiidDdlLexicon.SchemaElement.TYPE, SchemaElementType.VIRTUAL.toDdl());
        assertProperty(tableNode, TeiidDdlLexicon.CreateTable.INCLUDE_LOCAL_KEYWORD, true);
        assertThat(tableNode.getProperty(TeiidDdlLexicon.CreateTable.PRIMARY_KEY_COLUMNS), is(notNullValue()));
        assertProperty(tableNode, TeiidDdlLexicon.CreateTable.INCLUDE_ON_COMMIT_CLAUSE, true);

        final List<AstNode> kids = tableNode.getChildren();
        assertThat(kids.size(), is(2));

        boolean foundC1 = false;
        boolean foundC2 = false;

        for (final AstNode tempTableElement : kids) {
            assertMixinType(tempTableElement, TeiidDdlLexicon.CreateTable.TEMP_TABLE_ELEMENT);

            if (!foundC1 && tempTableElement.getName().equals("c1")) {
                assertProperty(tempTableElement, StandardDdlLexicon.DATATYPE_NAME, TeiidDataType.INTEGER.toDdl());
                assertProperty(tempTableElement, StandardDdlLexicon.NULLABLE, "NULL");
                assertProperty(tempTableElement, TeiidDdlLexicon.CreateTable.INCLUDE_SERIAL_ALIAS, true);
                foundC1 = true;
            } else if (!foundC2 && tempTableElement.getName().equals("c2")) {
                assertProperty(tempTableElement, StandardDdlLexicon.DATATYPE_NAME, TeiidDataType.INTEGER.toDdl());
                assertProperty(tempTableElement, StandardDdlLexicon.NULLABLE, "NULL");
                assertProperty(tempTableElement, TeiidDdlLexicon.CreateTable.INCLUDE_SERIAL_ALIAS, false);
                foundC2 = true;
            } else {
                fail();
            }
        }
    }

    @Test
    public void shouldParseLocalTemporaryTableWithouLocalKeyword() {
        final String content = "Create TEMPORARY table x (c1.x boolean, c2 byte)";
        final AstNode tableNode = this.parser.parse(getTokens(content), this.rootNode);
        assertThat(tableNode, is(notNullValue()));
        assertThat(tableNode.getName(), is("x"));
        assertMixinType(tableNode, TeiidDdlLexicon.CreateTable.LOCAL_TEMP_TABLE_STATEMENT);
        assertProperty(tableNode, TeiidDdlLexicon.SchemaElement.TYPE, SchemaElementType.VIRTUAL.toDdl());
        assertProperty(tableNode, TeiidDdlLexicon.CreateTable.INCLUDE_LOCAL_KEYWORD, false);
        assertProperty(tableNode, TeiidDdlLexicon.CreateTable.PRIMARY_KEY_COLUMNS, null);
        assertProperty(tableNode, TeiidDdlLexicon.CreateTable.INCLUDE_ON_COMMIT_CLAUSE, false);
    }

}
