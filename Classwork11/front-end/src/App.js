import './App.css';
import React from 'react';

function App() {
  const [username, setUsername] = React.useState('');
  const [password, setPassword] = React.useState('');
  const [isLoggedIn, setIsLoggedIn] = React.useState(false);
  const [error, setError] = React.useState(null);

  const handleSubmit = () => {
    const body = {
      username: username,
      password: password,
    };
    const settings = {
      method: 'post',
      body: JSON.stringify(body),
    };
    fetch('/logIn', settings)
      .then(res => res.json())
      .then(data => {
        if (data.isLoggedIn) {
          setIsLoggedIn(true);
        } else if (data.error) {
          setError(data.error);
        }
      })
      .catch(e => console.log(e));
  };

  if (isLoggedIn) {
    return (
      <div>
        <h1>Welcome {username}!</h1>
      </div>
    );
  }

  return (
    <div>
      <div>Username <input value={username} onChange={e => setUsername(e.target.value)} /></div>
      <div>Password <input type="password" value={password} onChange={e => setPassword(e.target.value)} /></div>
      <button onClick={handleSubmit}>Submit</button>
      {error}
    </div >
  );
}

export default App;
