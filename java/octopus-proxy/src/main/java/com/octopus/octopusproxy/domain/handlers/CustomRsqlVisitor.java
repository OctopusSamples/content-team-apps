package com.octopus.octopusproxy.domain.handlers;

import cz.jirutka.rsql.parser.ast.AndNode;
import cz.jirutka.rsql.parser.ast.ComparisonNode;
import cz.jirutka.rsql.parser.ast.NoArgRSQLVisitorAdapter;
import cz.jirutka.rsql.parser.ast.OrNode;

/**
 * A simple visitor over a RSQL filter that understands limiting by name and instance.
 */
public class CustomRsqlVisitor extends NoArgRSQLVisitorAdapter {

  private String instanceArgument;
  private String nameArgument;

  private int andCount;

  /**
   * The instance to query against.
   *
   * @return The instance to query against.
   */
  public String getInstanceArgument() {
    return instanceArgument;
  }

  /**
   * The space name to return.
   *
   * @return The space name to return.
   */
  public String getNameArgument() {
    return nameArgument;
  }

  /**
   * This must be 1 to be a valid filter.
   *
   * @return The number of and nodes that were encountered.
   */
  public int getAndCount() {
    return andCount;
  }

  /**
   * Parses anded nodes.
   *
   * @param node The RSQ "and" node.
   * @return null
   */
  @Override
  public Object visit(final AndNode node) {
    andCount = getAndCount() + 1;
    node.getChildren().forEach(c -> c.accept(this));
    return null;
  }

  /**
   * We don't support or logic for this filter.
   *
   * @param node The RSQL "or" node.
   * @return null
   */
  @Override
  public Object visit(final OrNode node) {
    return null;
  }

  @Override
  public Object visit(final ComparisonNode node) {
    if (node.getSelector().equals("instance")
        && node.getOperator().getSymbol().equals("==")
        && !node.getArguments().isEmpty()) {
      instanceArgument = node.getArguments().get(0);
    }

    if (node.getSelector().equals("name")
        && node.getOperator().getSymbol().equals("==")
        && !node.getArguments().isEmpty()) {
      nameArgument = node.getArguments().get(0);
    }

    return null;
  }
}
