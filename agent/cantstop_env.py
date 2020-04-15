import gym
import numpy as np
from gym import spaces, utils

from py4j.java_gateway import JavaGateway


class CantStopEnv(gym.Env):
    """An environment for the board game Can't Stop. Before starting this env,
    make sure the java gateway is already started."""

    metadata = {'render.modes': ['human']}

    def __init__(self):
        self.gateway = JavaGateway()

        # actions of the format : an array of size 13 [0,1,0,0,1...] where arr[i]
        # can only have one of three values 0,1,2 where arr[4] = 2 means we have
        # two groups of 4. To the action space, we also included one binary variable
        # to represent the agent's decision to stop or not
        self.action_space = spaces.Discrete(156)

        # For the observation state, we use the unique index of the CantStopState
        # well this may be too compact, thus hindering learning but at least
        # the index structured well enough to be useful and the result of a roll
        # like [2,4,4,6] means we roll 4 dice and that's the result
        ii32 = np.iinfo(np.int32)
        high = np.array([7,7,7,7,ii32.max])
        self.observation_space = spaces.MultiDiscrete(high)

        # reward will be the number of steps
        self.gateway.entry_point.init()
        self.time = 1

    def process_action(self, action):
        stop = 0
        if action > 77:
            stop = 1
            action -= 78

        move = [0 for i in range(13)]
        prev = 0
        curr = 12
        diff = 11
        level = 0
        for i in range(11):
            if action < curr:
                break
            else:
                level += 1
                prev = curr
                curr += diff
                diff -= 1
        if level == 0:
            move[action+1] = 2
        else:
            move[level] = 1
            action -= (prev - 1)
            move[level + action] = 1

        return (move, stop)

    def step(self, action):

        move, stop = self.process_action(action)

        jvm = self.gateway.jvm
        l = jvm.java.util.ArrayList()
        for a in move:
            l.append(a)

        self.gateway.entry_point.step(l, stop)
        reward = 1
        state = self.gateway.entry_point.getState()
        roll = self.gateway.entry_point.getRoll()
        done = self.gateway.entry_point.isDone()

        roll_list = []
        for i in range(roll.size()):
            roll_list.append(roll.pop(0))

        return (np.append(np.array(roll_list), state), reward, done, {})

    def reset(self):
        state = self.gateway.entry_point.reset()
        roll = self.gateway.entry_point.getRoll()

        return np.append(roll, state)

    def render(self, mode='human', close=False):
        print('Num of time steps: {}'.format(self.time))

    def close(self):
        pass
