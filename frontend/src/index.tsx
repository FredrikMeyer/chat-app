import React from 'react';
import ReactDOM from 'react-dom';
import './index.scss';
import App from './App';
import { ApolloProvider } from '@apollo/react-hooks'
import * as serviceWorker from './serviceWorker';
import { ApolloClient } from 'apollo-client';
import { InMemoryCache } from 'apollo-cache-inmemory';
import { HttpLink } from 'apollo-link-http';
import { onError } from 'apollo-link-error';
import { ApolloLink } from 'apollo-link';
import { WebSocketLink } from 'apollo-link-ws';
import { split } from 'apollo-link';
import { getMainDefinition } from 'apollo-utilities';

const wsLink = new WebSocketLink({
    uri: `ws://10.0.1.16:8888/ws`,
    options: {
        reconnect: true
    }
});

const httpLink = new HttpLink({
    uri: 'http://10.0.1.16:8888/graphql',
    credentials: 'same-origin'
})

const link = split(
    // split based on operation type
    ({ query }) => {
        const definition = getMainDefinition(query);
        console.log(query, definition)
        return (
            definition.kind === 'OperationDefinition' &&
                definition.operation === 'subscription'
        );
    },
    wsLink,
    httpLink,
);

const client = new ApolloClient({
    link: ApolloLink.from([
        onError(({ graphQLErrors, networkError }) => {
            if (graphQLErrors)
                graphQLErrors.forEach(({ message, locations, path }) =>
                    console.log(
                        `[GraphQL error]: Message: ${message}, Location: ${locations}, Path: ${path}`,
                    ),
                );
            if (networkError) console.log(`[Network error]: ${networkError}`);
        }),
        link
    ]),
    cache: new InMemoryCache()
});

const AppWithApollo = () => (
    <ApolloProvider client={client}>
        <App />
    </ApolloProvider>
)

ReactDOM.render(<AppWithApollo />, document.getElementById('root'));

// If you want your app to work offline and load faster, you can change
// unregister() to register() below. Note this comes with some pitfalls.
// Learn more about service workers: https://bit.ly/CRA-PWA
serviceWorker.unregister();
