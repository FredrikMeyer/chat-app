import React, { useState } from 'react';
import gql from 'graphql-tag'
import { useSubscription, useMutation } from '@apollo/react-hooks'
import './chatWindow.scss'

const MESSAGES = gql`
    subscription {
        messages {
            from
            message
        }
    }
`

const SEND_MESSAGE = gql`
    mutation($from: String!, $message: String!) {
        postMessage(from: $from, message: $message) {
            message
            from
        }
    }
`;

type Data = {
    messages : Message
}
type Message = {
    from: string,
    message: string,
    id: number
}

const NameAndInput = () => {
    const [name, setName] = useState<string>("")
    const [message, setMessage] = useState<string>("")

    const [sendMessage] = useMutation<Data>(SEND_MESSAGE)

    const onSubmit = (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault()
        sendMessage({variables: { from: name, message}});
        setMessage("");
    }

    return (
        <div className="chatWindow--nameAndInput">
            <form onSubmit={onSubmit}>
                <NameBox name={name} setName={setName} />
                <InputBox message={message} setMessage={setMessage} />
                <button type="submit">Send</button>
            </form>
        </div>
    )
}

const NameBox = ({name, setName }: { name: string, setName: (name: string) => void}) => {
    return (
        <div>
            <span>Navn: </span>
            <input type="text" value={name} onChange={e => setName(e.target.value)} />
        </div>
    )
}

const InputBox = ({message, setMessage}: {message: string, setMessage: (message: string) => void}) => {
    return (
        <div>
            <span>Melding: </span>
            <input type="text" value={message} onChange={e => setMessage(e.target.value)} />
        </div>
    )
}

const Chats = () => {
    const [messages, setMessages] = useState<Message[]>([])
    const { loading} = useSubscription<Data>(MESSAGES, {
        onSubscriptionData: ({ subscriptionData }) => {
            if (subscriptionData.data) {
                const { from, message, id } = subscriptionData.data.messages
                setMessages([...messages, { from, message, id }])
            }
        }
    })

    if (loading) return <span>"Ingen meldinger mottatt."</span>

    return <div className="messages">
    { messages.map(m => {
        return (
            <div key={m.id}>
                <b>{m.from}: </b>
                <span>{m.message}</span>
            </div>)
        })}
    </div>
    
}

const ChatWindow = () => {
    return (
        <div className="chatWindow">
            <NameAndInput />
            <Chats />
        </div>
    )
}

export default ChatWindow
