/*
 * Copyright (C) 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.appformer.flow.lang;

import java.util.Objects;

class Token {

    final String    symbol;
    final TokenKind kind;

    Token( final String symbol,
                  final TokenKind kind ) {
        this.symbol = symbol;
        this.kind = kind;
    }

    boolean isSymbol() {
        return TokenKind.Symbol.equals( kind );
    }

    boolean isSymbol( final String symbol ) {
        return isSymbol() && Objects.equals( this.symbol, symbol );
    }

    boolean isKeyword() {
        return TokenKind.Keyword.equals( kind );
    }

    boolean isKeyword( final String keyword ) {
        return isKeyword() && Objects.equals( this.symbol, keyword );
    }

    boolean isOperator() {
        return TokenKind.Operator.equals( kind );
    }

    boolean isOperator( final String operator ) {
        return isOperator() && Objects.equals( this.symbol, operator );
    }

    boolean isConstant() {
        return TokenKind.Constant.equals( kind );
    }

    boolean isConstant( final String constant ) {
        return isConstant() && Objects.equals( this.symbol, constant );
    }

    boolean isIdentifier() {
        return TokenKind.Identifier.equals( kind );
    }

    boolean isIdentifier( final String identifier ) {
        return isIdentifier() && Objects.equals( this.symbol, identifier );
    }

    boolean isLiteral() {
        return isConstant() || isKeyword( "true" ) || isKeyword( "false" );
    }

    static Token symbol( final String symbol ) {
        return new Token( symbol,
                          TokenKind.Symbol );
    }

    static Token keyword( final String symbol ) {
        return new Token( symbol,
                          TokenKind.Keyword );
    }

    static Token operator( final String symbol ) {
        return new Token( symbol,
                          TokenKind.Operator );
    }

    static Token constant( final String symbol ) {
        return new Token( symbol,
                          TokenKind.Constant );
    }

    static Token identifier( final String symbol ) {
        return new Token( symbol,
                          TokenKind.Identifier );
    }

    @Override
    public boolean equals( final Object obj ) {
        if ( obj instanceof Token ) {
            final Token other = (Token) obj;
            return kind.equals( other.kind ) && symbol.equals( other.symbol );
        }
        else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return kind.hashCode() ^ symbol.hashCode();
    }

    @Override
    public String toString() {
        return kind.toString() + "[" + symbol + "]";
    }

    static enum TokenKind {
        Keyword, Identifier, Constant, Operator, Symbol
    }

}
