import { Link, Route, Routes } from 'react-router-dom'
import { useState } from 'react'
import ReactFlow from 'react-flow-renderer'

function Landing(){return <div><h1 className='text-3xl font-bold'>Monolith Breaker</h1><p>Deterministic impact analysis for Java monoliths.</p></div>}

function Project(){
  const [name,setName]=useState('Sample');const [projectId,setProjectId]=useState('');const [msg,setMsg]=useState('');
  const create=async()=>{const r=await fetch('/api/projects',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({name})});const j=await r.json();setProjectId(j.data.projectId)}
  const upload=async(e)=>{const f=e.target.files[0];const fd=new FormData();fd.append('file',f);const r=await fetch(`/api/projects/${projectId}/upload`,{method:'POST',body:fd});const j=await r.json();setMsg(JSON.stringify(j.data))}
  return <div className='space-y-2'><input className='border p-2' value={name} onChange={e=>setName(e.target.value)}/><button className='bg-blue-600 text-white px-3 py-2' onClick={create}>Create Project</button><div>{projectId}</div>{projectId&&<input type='file' onChange={upload}/>}<div>{msg}</div></div>
}

function Progress(){
  const [runId,setRunId]=useState('');const [status,setStatus]=useState('');
  const poll=async()=>{const j=await (await fetch(`/api/analysis/${runId}/status`)).json();setStatus(JSON.stringify(j.data))}
  return <div><input className='border p-2' value={runId} onChange={e=>setRunId(e.target.value)} placeholder='runId'/><button className='bg-slate-700 text-white px-3 py-2' onClick={poll}>Poll</button><pre>{status}</pre></div>
}

function Dashboard(){
  const [runId,setRunId]=useState('');const [risk,setRisk]=useState([]);const [nodes,setNodes]=useState([]);const [edges,setEdges]=useState([]);const [com,setCom]=useState([]);const [ai,setAi]=useState(null);
  const load=async()=>{setRisk((await (await fetch(`/api/analysis/${runId}/risk`)).json()).data);setNodes((await (await fetch(`/api/analysis/${runId}/nodes`)).json()).data);setEdges((await (await fetch(`/api/analysis/${runId}/edges`)).json()).data);setCom((await (await fetch(`/api/analysis/${runId}/communities`)).json()).data)}
  const split=async()=>setAi((await (await fetch(`/api/analysis/${runId}/ai/split`,{method:'POST'})).json()).data)
  const top = risk.slice(0,150)
  const rfNodes = top.map((r,i)=>({id:r.node,data:{label:`${r.node} (${r.riskScore})`},position:{x:(i%10)*180,y:Math.floor(i/10)*90}}))
  const rfEdges = edges.filter(e=>top.some(t=>t.node===e.from)&&top.some(t=>t.node===e.to)).slice(0,200).map((e,i)=>({id:`e${i}`,source:e.from,target:e.to,label:e.type}))
  return <div className='space-y-4'><div><input className='border p-2' value={runId} onChange={e=>setRunId(e.target.value)}/><button className='bg-green-600 text-white px-3 py-2' onClick={load}>Load</button></div>
    <h2 className='font-semibold'>Risk Leaderboard</h2><table className='text-sm'><tbody>{top.map(r=><tr key={r.node}><td>{r.node}</td><td>{r.riskScore}</td><td>{r.label}</td></tr>)}</tbody></table>
    <div style={{height:400}}><ReactFlow nodes={rfNodes} edges={rfEdges}/></div>
    <h2 className='font-semibold'>Communities</h2><pre>{JSON.stringify(com,null,2)}</pre>
    <button className='bg-purple-700 text-white px-3 py-2' onClick={split}>AI Split</button>{ai&&<pre>{JSON.stringify(ai,null,2)}</pre>}
  </div>
}

export default function App(){
  return <div className='p-6 space-y-4'><nav className='space-x-4'><Link to='/'>Landing</Link><Link to='/project'>Create+Upload</Link><Link to='/progress'>Progress</Link><Link to='/dashboard'>Dashboard</Link></nav><Routes><Route path='/' element={<Landing/>}/><Route path='/project' element={<Project/>}/><Route path='/progress' element={<Progress/>}/><Route path='/dashboard' element={<Dashboard/>}/></Routes></div>
}
