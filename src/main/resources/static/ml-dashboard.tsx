"use client"

import { useState, useEffect } from "react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Progress } from "@/components/ui/progress"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Brain, TrendingUp, Shield, Target, Activity, AlertTriangle, CheckCircle, Clock, Zap } from "lucide-react"

interface MLPrediction {
  id: string
  playerId: string
  sessionId: string
  type: string
  output: any
  confidence: number
  predictedAt: string
}

interface Player {
  id: string
  name: string
  position: string
  jerseyNumber: number
}

export default function MLDashboard() {
  const [players, setPlayers] = useState<Player[]>([])
  const [selectedPlayer, setSelectedPlayer] = useState<string>("")
  const [predictions, setPredictions] = useState<MLPrediction[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    fetchPlayers()
  }, [])

  useEffect(() => {
    if (selectedPlayer) {
      fetchPlayerPredictions(selectedPlayer)
    }
  }, [selectedPlayer])

  const fetchPlayers = async () => {
    try {
      const response = await fetch("/api/players/active")
      const data = await response.json()
      setPlayers(data)
      if (data.length > 0) {
        setSelectedPlayer(data[0].id)
      }
    } catch (error) {
      console.error("Error fetching players:", error)
      setError("Failed to fetch players")
    }
  }

  const fetchPlayerPredictions = async (playerId: string) => {
    try {
      setLoading(true)
      const response = await fetch(`/api/ml/predictions/player/${playerId}`)
      const data = await response.json()
      setPredictions(data)
    } catch (error) {
      console.error("Error fetching predictions:", error)
      setError("Failed to fetch predictions")
    } finally {
      setLoading(false)
    }
  }

  const runInjuryPrediction = async (playerId: string) => {
    try {
      setLoading(true)
      const response = await fetch(`/api/ml/injury-risk/${playerId}`, {
        method: "POST",
      })
      const prediction = await response.json()
      setPredictions((prev) => [prediction, ...prev])
    } catch (error) {
      console.error("Error running injury prediction:", error)
      setError("Failed to run injury prediction")
    } finally {
      setLoading(false)
    }
  }

  const runPerformanceOptimization = async (playerId: string) => {
    try {
      setLoading(true)
      const response = await fetch(`/api/ml/optimize-performance/${playerId}`, {
        method: "POST",
      })
      const prediction = await response.json()
      setPredictions((prev) => [prediction, ...prev])
    } catch (error) {
      console.error("Error running performance optimization:", error)
      setError("Failed to run performance optimization")
    } finally {
      setLoading(false)
    }
  }

  const runFatigueDetection = async (playerId: string) => {
    try {
      setLoading(true)
      // Assuming we have a session ID - in real app this would be dynamic
      const sessionId = "session_001"
      const response = await fetch(`/api/ml/fatigue-detection/${playerId}/session/${sessionId}`, {
        method: "POST",
      })
      const prediction = await response.json()
      setPredictions((prev) => [prediction, ...prev])
    } catch (error) {
      console.error("Error running fatigue detection:", error)
      setError("Failed to run fatigue detection")
    } finally {
      setLoading(false)
    }
  }

  const getInjuryRiskColor = (riskLevel: string) => {
    switch (riskLevel) {
      case "HIGH":
        return "text-red-500 bg-red-50 border-red-200"
      case "MODERATE":
        return "text-yellow-600 bg-yellow-50 border-yellow-200"
      case "LOW":
        return "text-blue-500 bg-blue-50 border-blue-200"
      default:
        return "text-green-500 bg-green-50 border-green-200"
    }
  }

  const getFatigueColor = (category: string) => {
    switch (category) {
      case "SEVERE":
        return "text-red-600 bg-red-50"
      case "HIGH":
        return "text-orange-600 bg-orange-50"
      case "MODERATE":
        return "text-yellow-600 bg-yellow-50"
      default:
        return "text-green-600 bg-green-50"
    }
  }

  const getPriorityColor = (priority: string) => {
    switch (priority) {
      case "HIGH":
        return "bg-red-500"
      case "MEDIUM":
        return "bg-yellow-500"
      default:
        return "bg-green-500"
    }
  }

  const renderInjuryPrediction = (prediction: MLPrediction) => {
    const output = prediction.output
    const riskLevel = output.riskLevel || "MINIMAL"
    const injuryRisk = (output.injuryRisk * 100).toFixed(1)

    return (
      <Card className={`border-2 ${getInjuryRiskColor(riskLevel)}`}>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Shield className="w-5 h-5" />
            Injury Risk Assessment
            <Badge variant="outline" className="ml-auto">
              {(prediction.confidence * 100).toFixed(0)}% confidence
            </Badge>
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="flex items-center justify-between">
            <span className="text-sm font-medium">Risk Level:</span>
            <Badge className={getInjuryRiskColor(riskLevel)}>
              {riskLevel} ({injuryRisk}%)
            </Badge>
          </div>

          <div>
            <span className="text-sm font-medium mb-2 block">Key Risk Factors:</span>
            <ul className="text-sm space-y-1">
              {output.keyFactors?.map((factor: string, index: number) => (
                <li key={index} className="flex items-center gap-2">
                  <AlertTriangle className="w-3 h-3 text-yellow-500" />
                  {factor}
                </li>
              ))}
            </ul>
          </div>

          <div>
            <span className="text-sm font-medium mb-2 block">Recommendations:</span>
            <ul className="text-sm space-y-1">
              {output.recommendations?.map((rec: string, index: number) => (
                <li key={index} className="flex items-center gap-2">
                  <CheckCircle className="w-3 h-3 text-green-500" />
                  {rec}
                </li>
              ))}
            </ul>
          </div>
        </CardContent>
      </Card>
    )
  }

  const renderPerformanceOptimization = (prediction: MLPrediction) => {
    const output = prediction.output
    const priority = output.priority || "LOW"

    return (
      <Card className="border-2 border-blue-200 bg-blue-50">
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <TrendingUp className="w-5 h-5" />
            Performance Optimization
            <Badge variant="outline" className="ml-auto">
              {(prediction.confidence * 100).toFixed(0)}% confidence
            </Badge>
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="flex items-center justify-between">
            <span className="text-sm font-medium">Priority:</span>
            <Badge className={`text-white ${getPriorityColor(priority)}`}>{priority}</Badge>
          </div>

          <div>
            <span className="text-sm font-medium mb-2 block">Training Focus:</span>
            <div className="flex flex-wrap gap-1">
              {output.trainingFocus?.map((focus: string, index: number) => (
                <Badge key={index} variant="secondary" className="text-xs">
                  {focus}
                </Badge>
              ))}
            </div>
          </div>

          <div>
            <span className="text-sm font-medium mb-2 block">Recommendations:</span>
            <ul className="text-sm space-y-1">
              {output.recommendations?.map((rec: string, index: number) => (
                <li key={index} className="flex items-center gap-2">
                  <Target className="w-3 h-3 text-blue-500" />
                  {rec}
                </li>
              ))}
            </ul>
          </div>

          {output.targetMetrics && (
            <div>
              <span className="text-sm font-medium mb-2 block">Target Improvements:</span>
              <div className="grid grid-cols-2 gap-2 text-xs">
                {Object.entries(output.targetMetrics).map(([key, value]) => (
                  <div key={key} className="bg-white p-2 rounded">
                    <div className="font-medium">{key.replace("target", "").replace(/([A-Z])/g, " $1")}</div>
                    <div className="text-blue-600">{typeof value === "number" ? value.toFixed(1) : value}</div>
                  </div>
                ))}
              </div>
            </div>
          )}
        </CardContent>
      </Card>
    )
  }

  const renderFatiguePrediction = (prediction: MLPrediction) => {
    const output = prediction.output
    const fatigueCategory = output.fatigueCategory || "LOW"
    const fatigueLevel = ((output.fatigueLevel || 0) * 100).toFixed(1)

    return (
      <Card className={`border-2 ${getFatigueColor(fatigueCategory)} border-opacity-50`}>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Activity className="w-5 h-5" />
            Fatigue Detection
            <Badge variant="outline" className="ml-auto">
              {(prediction.confidence * 100).toFixed(0)}% confidence
            </Badge>
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="flex items-center justify-between">
            <span className="text-sm font-medium">Fatigue Level:</span>
            <Badge className={getFatigueColor(fatigueCategory)}>
              {fatigueCategory} ({fatigueLevel}%)
            </Badge>
          </div>

          <div>
            <Progress value={Number.parseFloat(fatigueLevel)} className="h-2" />
            <div className="flex justify-between text-xs text-gray-500 mt-1">
              <span>Low</span>
              <span>High</span>
            </div>
          </div>

          <div>
            <span className="text-sm font-medium mb-2 block">Immediate Actions:</span>
            <ul className="text-sm space-y-1">
              {output.immediateActions?.map((action: string, index: number) => (
                <li key={index} className="flex items-center gap-2">
                  <Zap className="w-3 h-3 text-orange-500" />
                  {action}
                </li>
              ))}
            </ul>
          </div>

          <div>
            <span className="text-sm font-medium mb-2 block">Recovery Protocols:</span>
            <ul className="text-sm space-y-1">
              {output.recoveryProtocols?.map((protocol: string, index: number) => (
                <li key={index} className="flex items-center gap-2">
                  <Clock className="w-3 h-3 text-blue-500" />
                  {protocol}
                </li>
              ))}
            </ul>
          </div>

          {output.estimatedRecoveryTime && (
            <div className="bg-white p-3 rounded border">
              <span className="text-sm font-medium">Estimated Recovery Time:</span>
              <div className="text-lg font-bold text-blue-600">{output.estimatedRecoveryTime}</div>
            </div>
          )}
        </CardContent>
      </Card>
    )
  }

  const renderPrediction = (prediction: MLPrediction) => {
    switch (prediction.type) {
      case "INJURY_RISK":
        return renderInjuryPrediction(prediction)
      case "PERFORMANCE_DECLINE":
        return renderPerformanceOptimization(prediction)
      case "FATIGUE_LEVEL":
        return renderFatiguePrediction(prediction)
      default:
        return (
          <Card>
            <CardContent className="p-4">
              <div className="text-sm text-gray-500">Unknown prediction type: {prediction.type}</div>
            </CardContent>
          </Card>
        )
    }
  }

  return (
    <div className="min-h-screen bg-gray-50 p-6">
      <div className="max-w-7xl mx-auto">
        {/* Header */}
        <div className="flex justify-between items-center mb-8">
          <div>
            <h1 className="text-3xl font-bold text-gray-900 flex items-center gap-2">
              <Brain className="w-8 h-8 text-blue-600" />
              AI-Powered Football Analytics
            </h1>
            <p className="text-gray-600">Machine Learning insights for player performance and safety</p>
          </div>
        </div>

        {/* Player Selection */}
        <Card className="mb-6">
          <CardHeader>
            <CardTitle>Select Player for AI Analysis</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex flex-wrap gap-2 mb-4">
              {players.map((player) => (
                <Button
                  key={player.id}
                  variant={selectedPlayer === player.id ? "default" : "outline"}
                  onClick={() => setSelectedPlayer(player.id)}
                  className="flex items-center gap-2"
                >
                  <span className="font-mono text-sm">#{player.jerseyNumber}</span>
                  {player.name}
                  <Badge variant="secondary" className="text-xs">
                    {player.position}
                  </Badge>
                </Button>
              ))}
            </div>

            {selectedPlayer && (
              <div className="flex gap-2 pt-4 border-t">
                <Button
                  onClick={() => runInjuryPrediction(selectedPlayer)}
                  disabled={loading}
                  className="flex items-center gap-2"
                >
                  <Shield className="w-4 h-4" />
                  Analyze Injury Risk
                </Button>
                <Button
                  onClick={() => runPerformanceOptimization(selectedPlayer)}
                  disabled={loading}
                  variant="outline"
                  className="flex items-center gap-2"
                >
                  <TrendingUp className="w-4 h-4" />
                  Optimize Performance
                </Button>
                <Button
                  onClick={() => runFatigueDetection(selectedPlayer)}
                  disabled={loading}
                  variant="outline"
                  className="flex items-center gap-2"
                >
                  <Activity className="w-4 h-4" />
                  Detect Fatigue
                </Button>
              </div>
            )}
          </CardContent>
        </Card>

        {/* Error Alert */}
        {error && (
          <Alert className="mb-6 border-red-200 bg-red-50">
            <AlertTriangle className="h-4 w-4" />
            <AlertDescription>{error}</AlertDescription>
          </Alert>
        )}

        {/* Loading State */}
        {loading && (
          <Card className="mb-6">
            <CardContent className="flex items-center justify-center p-8">
              <div className="flex items-center gap-3">
                <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-blue-600"></div>
                <span>Running AI analysis...</span>
              </div>
            </CardContent>
          </Card>
        )}

        {/* Predictions */}
        <div className="space-y-6">
          {predictions.length === 0 && !loading && (
            <Card>
              <CardContent className="text-center py-12">
                <Brain className="w-12 h-12 text-gray-400 mx-auto mb-4" />
                <h3 className="text-lg font-medium text-gray-900 mb-2">No AI Predictions Yet</h3>
                <p className="text-gray-500 mb-4">Select a player and run AI analysis to get intelligent insights</p>
              </CardContent>
            </Card>
          )}

          {predictions.map((prediction) => (
            <div key={prediction.id}>
              {renderPrediction(prediction)}
              <div className="text-xs text-gray-500 mt-2 text-right">
                Predicted at: {new Date(prediction.predictedAt).toLocaleString()}
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}
